package FiveStage
import chisel3._
import chisel3.util.MuxLookup
import chisel3.experimental.MultiIOModule


class ALU extends MultiIOModule {


  val io = IO(
    new Bundle {

      val op1 = Input(UInt(32.W))
      val op2 = Input(UInt(32.W))
      val aluOp = Input(UInt(4.W))
   
      val aluResult = Output(UInt(32.W))
    }
  )

  val ALUopMap = Array(
  ALUOps.ADD    -> (io.op1 + io.op2),
  ALUOps.SUB    -> (io.op1 - io.op2),
  ALUOps.AND    -> (io.op1 & io.op2),
  ALUOps.OR     -> (io.op1 | io.op2),
  ALUOps.XOR    -> (io.op1 ^ io.op2),
  ALUOps.SLT    -> (io.op1.asSInt < io.op2.asSInt).asUInt,
  ALUOps.SLL    -> (io.op1 << io.op2(4, 0)),
  ALUOps.SLTU   -> ((io.op1 < io.op2).asUInt),
  ALUOps.SRL    -> (io.op1 >> io.op2(4, 0)),
  ALUOps.SRA    -> (io.op1.asSInt >> io.op2(4, 0)).asUInt,
  ALUOps.COPY_A -> (io.op1),
  ALUOps.COPY_B -> (io.op2),
  ALUOps.DC     -> (0.U)
  )

  // MuxLookup API: https://github.com/freechipsproject/chisel3/wiki/Muxes-and-Input-Selection#muxlookup
  io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUopMap)

  
}
