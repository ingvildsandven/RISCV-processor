package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.util.MuxLookup
import chisel3.experimental.MultiIOModule


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
      val input  = Input(new Instruction)
      val PCIn   = Input(UInt())
      val writeEnable  = Input(Bool())
      val writeAddress = Input(UInt(5.W))
      val writeData    = Input(UInt(32.W))

      val PCOut  = Output(UInt())

      val controlSignals = Output(new ControlSignals)
      val branchType     = Output(UInt(3.W))
      val op1Select      = Output(UInt(1.W))
      val op2Select      = Output(UInt(1.W))
      val immediate      = Output(SInt(32.W))
      val ALUop          = Output(UInt(4.W))

      val readData1  = Output(UInt(32.W))
      val readData2  = Output(UInt(32.W))
      val registerRd = Output(UInt(5.W))
      val registerRs1 = Output(UInt(5.W))
      val registerRs2 = Output(UInt(5.W))
      
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io


  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  //Register access
  registers.io.readAddress1 := io.input.registerRs1
  registers.io.readAddress2 := io.input.registerRs2

  registers.io.writeEnable  := io.writeEnable
  registers.io.writeAddress := io.writeAddress
  registers.io.writeData    := io.writeData

  // Gives decode the newest value when wb is writing the same reg decode is reading
  val bypassRs1 = io.writeEnable &&
    (io.writeAddress =/= 0.U) &&
    (io.writeAddress === io.input.registerRs1)

  io.readData1 := Mux(
    bypassRs1,
    io.writeData,
    registers.io.readData1
  )

  val bypassRs2 = io.writeEnable &&
    (io.writeAddress =/= 0.U) &&
    (io.writeAddress === io.input.registerRs2)

  io.readData2 := Mux(
    bypassRs2,
    io.writeData,
    registers.io.readData2
  ) 

  io.registerRd := io.input.registerRd
  io.registerRs1 := io.input.registerRs1
  io.registerRs2 := io.input.registerRs2

  // Decoding the input
  decoder.instruction := io.input
  io.branchType := decoder.branchType
  io.op1Select := decoder.op1Select
  io.op2Select := decoder.op2Select
  io.ALUop := decoder.ALUop
  io.controlSignals := decoder.controlSignals

  // selecting immediate type: 
  val immediateMap = Array(
  ImmFormat.ITYPE -> io.input.immediateIType,
  ImmFormat.STYPE -> io.input.immediateSType,
  ImmFormat.BTYPE -> io.input.immediateBType,
  ImmFormat.UTYPE -> io.input.immediateUType,
  ImmFormat.JTYPE -> io.input.immediateJType
  )

  io.immediate := MuxLookup(
    decoder.immType,
    0.S(32.W),
    immediateMap
  )

  // Passing the PC
  io.PCOut := io.PCIn
  
}
