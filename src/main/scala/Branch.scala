package FiveStage

import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule

class BranchUnit extends Module {
  val io = IO(new Bundle {
    val r1 = Input(UInt(32.W))
    val r2 = Input(UInt(32.W))
    val branchType = Input(UInt(3.W))

    val branchTaken = Output(Bool())
  })

  io.branchTaken := false.B
  switch(io.branchType){
    is(branchType.beq){
        io.branchTaken := io.r1 === io.r2
    }
    is(branchType.neq){
        io.branchTaken := io.r1 =/= io.r2
    }
    is(branchType.gte){
        io.branchTaken := io.r1.asSInt >= io.r2.asSInt
    }
    is(branchType.lt){
        io.branchTaken := io.r1.asSInt < io.r2.asSInt
    }
    is(branchType.gteu){
        io.branchTaken := io.r1 >= io.r2
    }
    is(branchType.ltu){
        io.branchTaken := io.r1 < io.r2
    }
    is(branchType.jump){
        io.branchTaken := true.B
    }

  }
}