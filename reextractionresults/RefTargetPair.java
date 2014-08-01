package com.deft.sarcasm.postprocess;

public class RefTargetPair<L, R> 
{
	private L left ;
	private R right ;
	
	public RefTargetPair(L left, R right) {
	    this.left = left;
	    this.right = right;
	  }

	  public L getLeft() { return left; }
	  public R getRight() { return right; }

	  @Override
	  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

	  @Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof RefTargetPair)) return false;
	    RefTargetPair pairo = (RefTargetPair) o;
	    return this.left.equals(pairo.getLeft()) &&
	           this.right.equals(pairo.getRight());
	  }
	  
	  public String toString()
	  {
		  return left.toString() +"\t" + right.toString() ;
	  }
}
