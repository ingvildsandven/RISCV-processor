package FiveStage

import chisel3._
import chisel3.util._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class CPU extends MultiIOModule {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  /**
    You need to create the classes for these yourself
    */
  val IFIDBarrier   = Module(new IFIDBarrier).io
  val IDEXBarrier   = Module(new IDEXBarrier).io
  val EXMEMBarrier  = Module(new EXMEMBarrier).io
  val MEMWBBarrier    = Module(new MEMWBBarrier).io

  val ID  = Module(new InstructionDecode)
  val IF  = Module(new InstructionFetch)
  val EX  = Module(new Execution)
  val MEM = Module(new MemoryFetch)
  // val WB  = Module(new Execute) (You may not need this one?)


  /**
    * Setup. You should not change this code
    */
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  /**
    spying stuff
    */
  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC  := IF.testHarness.PC


  // IF-ID Barrier connection
  IFIDBarrier.instructionIn := IF.io.output
  IFIDBarrier.PCIn          := IF.io.PC

  ID.io.input               := IFIDBarrier.instructionOut
  ID.io.PCIn                := IFIDBarrier.PCOut

  // ID-EX Barrier connection
  IDEXBarrier.PCIn             := ID.io.PCOut
  IDEXBarrier.controlSignalsIn := ID.io.controlSignals
  IDEXBarrier.branchTypeIn     := ID.io.branchType
  IDEXBarrier.op1SelectIn      := ID.io.op1Select
  IDEXBarrier.op2SelectIn      := ID.io.op2Select
  IDEXBarrier.immediateIn      := ID.io.immediate
  IDEXBarrier.ALUopIn          := ID.io.ALUop
  IDEXBarrier.readData1In      := ID.io.readData1
  IDEXBarrier.readData2In      := ID.io.readData2
  IDEXBarrier.registerRdIn     := ID.io.registerRd
  IDEXBarrier.registerRs1In    := ID.io.registerRs1
  IDEXBarrier.registerRs2In    := ID.io.registerRs2

  EX.io.PCIn           := IDEXBarrier.PCOut    
  EX.io.controlSignals := IDEXBarrier.controlSignalsOut
  EX.io.branchType     := IDEXBarrier.branchTypeOut
  EX.io.op1Select      := IDEXBarrier.op1SelectOut
  EX.io.op2Select      := IDEXBarrier.op2SelectOut
  EX.io.immediate      := IDEXBarrier.immediateOut
  EX.io.ALUop          := IDEXBarrier.ALUopOut
  EX.io.readData1      := IDEXBarrier.readData1Out
  EX.io.readData2      := IDEXBarrier.readData2Out
  EX.io.registerRd     := IDEXBarrier.registerRdOut
  EX.io.registerRs1    := IDEXBarrier.registerRs1Out
  EX.io.registerRs2    := IDEXBarrier.registerRs2Out

  // Implements stalling
  IF.io.stall               := EX.io.stall
  IFIDBarrier.stall         := EX.io.stall
  IDEXBarrier.stall         := EX.io.stall
  EXMEMBarrier.stall        := EX.io.stall

  IFIDBarrier.flush := EX.io.branchTaken && !EX.io.stall // IFID is flushed when EX is set
  IDEXBarrier.flush := EX.io.branchTaken && !EX.io.stall // Stalled branch does not redirect or flush instructions

  // EX to IF, jump connection
  IF.io.branchTaken := EX.io.branchTaken && !EX.io.stall
  IF.io.branchTarget := EX.io.branchTarget

  // EX-MEM Barrier Connection
  EXMEMBarrier.PCIn               := EX.io.PCOut
  EXMEMBarrier.aluResultIn        := EX.io.aluResult
  EXMEMBarrier.readData2In        := EX.io.readData2Out
  EXMEMBarrier.registerRdIn       := EX.io.registerRdOut
  EXMEMBarrier.controlSignalsIn   := EX.io.controlSignalsOut
     
  MEM.io.PCIn             := EXMEMBarrier.PCOut 
  MEM.io.aluResult        := EXMEMBarrier.aluResultOut 
  MEM.io.readData2        := EXMEMBarrier.readData2Out 
  MEM.io.registerRd       := EXMEMBarrier.registerRdOut  
  MEM.io.controlSignals   := EXMEMBarrier.controlSignalsOut 

  val jumpWriteData = Mux(
    EXMEMBarrier.controlSignalsOut.jump,
    EXMEMBarrier.PCOut + 4.U,
    MEM.io.aluResultOut
  )

  // Forward from mem
  EX.io.memRd         := EXMEMBarrier.registerRdOut
  EX.io.memRegWrite   := EXMEMBarrier.controlSignalsOut.regWrite
  EX.io.memRead       := EXMEMBarrier.controlSignalsOut.memRead
  EX.io.memResult     := jumpWriteData

  // MEM-WB
  MEMWBBarrier.aluResultIn      := jumpWriteData
  MEMWBBarrier.memoryDataIn     := MEM.io.memoryDataOut
  MEMWBBarrier.controlSignalsIn := MEM.io.controlSignalsOut
  MEMWBBarrier.registerRdIn     := MEM.io.registerRdOut
  

  // WB logic
  
  // Mux(condition, valueIfTrue, valueIfFalse)
  val writeData = Mux(
    MEMWBBarrier.controlSignalsOut.memRead,
    MEMWBBarrier.memoryDataOut,
    MEMWBBarrier.aluResultOut,
  )

  // Forward info from WB
  EX.io.wbRd          := MEMWBBarrier.registerRdOut
  EX.io.wbRegWrite    := MEMWBBarrier.controlSignalsOut.regWrite
  EX.io.wbResult      := writeData

  ID.io.writeEnable := MEMWBBarrier.controlSignalsOut.regWrite
  ID.io.writeAddress := MEMWBBarrier.registerRdOut
  ID.io.writeData := writeData

}
