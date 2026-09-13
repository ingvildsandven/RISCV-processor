package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


class Execution extends MultiIOModule {


  val io = IO(
    new Bundle {

      val op1 = Input(UInt())
      val op2 = Input(UInt())
   
      val PCIn = Input(UInt())
      val PCOut = Output(UInt())
    }
  )


  
}
