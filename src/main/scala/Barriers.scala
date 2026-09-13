package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class IFBarrier extends MultiIOModule{

    val io = IO(
        new Bundle{
            val instructionIn = Input(UInt())
            val instructionOut = Output(UInt())
            val PCIn = Input(UInt())
            val PCOut = Output(UInt())
        }
    )

    // Defining a register where PC is stored
    val PCDelayed = RegInit(0.U(32.W))

    PCDelayed := io.PCIn
    io.PCOut := PCDelayed

    io.instructionOut := io.instructionIn
}

class IDBarrier extends MultiIOModule{

    val io = IO(
        new Bundle{
            val ReadData1In = Input(UInt())
            val ReadData2In = Input(UInt())
            val WriteIn = Input(UInt())
            val SignextendIn = Input(UInt())

            val ReadData1Out = Output(UInt())
            val ReadData2Out = Output(UInt())
            val WriteOut = Output(UInt())
            val SignextendOut = Output(UInt())

            val PCIn = Input(UInt())
            val PCOut = Output(UInt())
        }
    )

    // Defining a register where PC is stored
    val PCDelayed = RegInit(0.U(32.W))

    PCDelayed := io.PCIn
    io.PCOut := PCDelayed

    io.ReadData1Out := io.ReadData1In
    io.ReadData2Out := io.ReadData2In
    io.WriteOut := io.WriteIn
    
}