package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {


  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    })

  val io = IO(
    new Bundle {
      val aluResult         = Input(UInt(32.W))
      val branchTaken       = Input(Bool())
      val branchTarget      = Input(UInt(32.W))
      val readData2         = Input(UInt(32.W))
      val registerRd        = Input(UInt(5.W))
      val controlSignals    = Input(new ControlSignals)

      val memoryDataOut        = Output(UInt(32.W))
      val aluResultOut         = Output(UInt(32.W))
      val controlSignalsOut    = Output(new ControlSignals)
      val registerRdOut           = Output(UInt(5.W))
    })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates

  //
  DMEM.io.dataIn      := io.readData2
  DMEM.io.dataAddress := io.aluResult(11, 0)
  DMEM.io.writeEnable := io.controlSignals.memWrite

  io.memoryDataOut      := DMEM.io.dataOut
  io.aluResultOut       := io.aluResult
  io.controlSignalsOut  := io.controlSignals
  io.registerRdOut      := io.registerRd

}
