package sportarray

import Skeleton.{DataType, PositionsData, ValuesData, WeightsData, PricesData}
import Skeleton.IsIdxElem
import IndicesObj.Index

import java.time.LocalDate
import shapeless._
import shapeless.ops.hlist._

object ArrayDefs {

  abstract class IsSpArr[A, T <: DataType, I0: IsIdxElem] {
    type Self = A
    type Idx0 = I0
    type M1
    def getIdx(self: A): Index[I0]
    def getElem(self: A, i: Int): M1
    def getNil(self: A): A
    def iloc[R](self: A, r: R)(implicit iLoc: ILoc[R, A]): iLoc.Out = iLoc.iloc(self, r)
    def ::(self: A, other: (I0, M1)): A
    def length(self: A): Int
    def toList(self: A): List[M1] = (for(i <- 0 to length(self)) yield (getElem(self, i))).toList
  }

  abstract class Is1dSpArr[A, T <: DataType, I0: IsIdxElem] extends IsSpArr[A, T, I0] {
    type M1 = T#T
    def shape(self: A): (Int) = length(self)
  }

  abstract class Is2dSpArr[A, T <: DataType, I0: IsIdxElem, I1: IsIdxElem, M1C[_ <: DataType, _]](
    implicit tc1d: Is1dSpArr[M1C[T, I1], T, I1]
  ) extends IsSpArr[A, T, I0] {
    type M1 = M1C[T, I1]
    def shape(self: A): (Int, Int) = 
      (length(self), tc1d.length(getElem(self, 0)))
  }

  trait ILoc[R, A] {
    type Out
    def iloc(self: A, ref: R): Out
  }
  object ILoc {
    type Aux[R0, A0, O0] = ILoc[R0, A0] { type Out = O0 }
    def apply[R, A](implicit tc: ILoc[R, A]): ILoc[R, A] = tc
    def instance[R, A, O](func: (A, R) => O): Aux[R, A, O] = new ILoc[R, A] {
      type Out = O
      def iloc(self: A, ref: R): Out = func(self, ref)
    }

    implicit def iLocForInt[A](implicit isArr: IsSpArr[A, _, _]): Aux[Int, A, isArr.M1] = instance(
      (self: A, ref: Int) => isArr.getElem(self, ref)
    )
  }

    //implicit def iLocForInt[A, T <: DataType](
      //implicit isArr: IsSpArr[A, T, _],
    //) = new ILoc[Int, A, T] { 
      //type Out = isArr.M1
      //def iloc(self: A, ref: Int): Out = isArr.getElem(self, ref)
    //}
    //implicit def iLocForList[A, T <: DataType, I0: IsIdxElem](implicit isArr: IsSpArr[A, T, I0]) = 
      //new ILoc[List[Int], A, T] { 
        //type Out = A
        //def iloc(self: A, ref: List[Int]): Out = {
          //val data: List[isArr.M1] = ref.map(isArr.getElem(self, _)).toList
          //val idx0: Index[I0] = isArr.getIdx(self)
          //val newIdx = Index(ref.map(idx0(_)))
          //newIdx.toList.zip(data).foldLeft(isArr.getNil(self))((a, b) => isArr.::(a, (b._1, b._2))) 
        //}
      //}
    //implicit def iLocForNull[A, T <: DataType](implicit isArr: IsSpArr[A, T, _]) = 
      //new ILoc[Null, A, T] { 
        //type Out = A
        //def iloc(self: A, ref: Null): Out = self
      //}
    //implicit def iLocForHNil[A, T <: DataType] = 
      //new ILoc[HNil, A, T] {
        //type Out = A
        //def iloc(self: A, ref: HNil): Out = self
      //}
    //implicit def iLocForHList[A, H, L <: HList, T <: DataType, O0](implicit 
      //isArr: IsSpArr[A, T, _],
      //ilocH: ILoc.Aux[H, A, T, O0],
      //outIsSpArr: IsSpArr[O0, T, _],
      //ilocOut: ILoc[L, O0, T],
    //) = new ILoc[H :: L, A, T] { 
      //type Out = O0
      //def iloc(self: A, ref: H :: L): Out = ilocH.iloc(self, ref.head) // ilocH.iloc(self, ref.head).toList.map(iLocOut.iloc(_, ref.tail))// need to map the iloc tail over every element
    //}
  //}

  object IsSpArrSyntax {
    implicit class IsSpArrOps[A, T <: DataType, I0](self: A)(implicit
      val tc: IsSpArr[A, T, I0] {type M1 = T#T},
    ) {
      def getElem(i: Int) = tc.getElem(self, i)
      def iloc[R](r: R)(implicit iLoc: ILoc[R, A]) = tc.iloc(self, r)
      def getNil = tc.getNil(self)
      def ::(other: (I0, tc.M1)): A = tc.::(self, other)
      def length: Int = tc.length(self)
      def toList: List[tc.M1] = tc.toList(self)
    }
    implicit class Is1dSpArrOps[A, T <: DataType, I0](self: A)(implicit 
      val tc: Is1dSpArr[A, T, I0] {type M1 = T#T},
    ) {
      def shape: Int = tc.shape(self)
      //def unapply: Option[((I0, T#T), A)] = tc1d.unapply(self) 
    }
    implicit class Is2dSpArrOps[A, T <: DataType, I0, I1, M1C[_ <: DataType, _]](self: A)(implicit 
      val tc: Is2dSpArr[A, T, I0, I1, M1C],
    ) {
      //def loc[R](r: R)(implicit locTc: tc2d.LocTC[R]) = tc2d.loc(self, r)
      def shape: (Int, Int) = tc.shape(self)
      //def unapply: Option[((I0, T#T), A)] = tc2d.unapply(self) 
    }
  }
}

