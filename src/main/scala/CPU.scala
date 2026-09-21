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

  IFIDBarrier.flush := EX.io.branchTaken // IFID is flushed when EX is set
  IDEXBarrier.flush := EX.io.branchTaken

  // EX to IF, jump connection
  IF.io.branchTaken := EX.io.branchTaken
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

  // MEM-WB
  MEMWBBarrier.PCIn             := MEM.io.PCOut
  MEMWBBarrier.aluResultIn      := MEM.io.aluResultOut
  MEMWBBarrier.memoryDataIn     := MEM.io.memoryDataOut
  MEMWBBarrier.controlSignalsIn := MEM.io.controlSignalsOut
  MEMWBBarrier.registerRdIn     := MEM.io.registerRdOut
  

  // WB logic
  
  //Mux(condition, valueIfTrue, valueIfFalse)
  // val writeData = Mux(
  //   MEMWBBarrier.controlSignalsOut.memRead,
  //   MEMWBBarrier.memoryDataOut,
  //   MEMWBBarrier.aluResultOut,
  // )

  // Three way mux for jump as well
  val writeData =  MuxCase(
    MEMWBBarrier.aluResultOut,  // Default
    Seq(
      MEMWBBarrier.controlSignalsOut.memRead ->  MEMWBBarrier.memoryDataOut,
      MEMWBBarrier.controlSignalsOut.jump    -> (MEMWBBarrier.PCOut + 4.U)
    )
  )

  ID.io.writeEnable := MEMWBBarrier.controlSignalsOut.regWrite
  ID.io.writeAddress := MEMWBBarrier.registerRdOut
  ID.io.writeData := writeData

 
}
