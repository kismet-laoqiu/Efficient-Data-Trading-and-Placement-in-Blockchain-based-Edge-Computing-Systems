/*********************************************
 * OPL 20.1.0.0 Model
 * Author: 15192
 * Creation Date: 2021��11��23�� at ����10:42:16
 *********************************************/

{int}D={i | i in 1..32};                 //data set

{int}P={4, 9, 13, 15, 18, 22,26,30,34,37,42,46};                  //producer set

   

{int}N={i| i in 1..48};

tuple arc                              //link set  ����յ�
{
 	int source;
 	int dest;	 
};

{arc}Udlink=...;//link ��˫��

float DataSize[D]=...;//data item �Ĵ�С

//float AccessT[N][P]=...;   // p1->n1�Ĵ���ʱ��

float AccessT[N][N]=...;   // p1->n1�Ĵ���ʱ��


float NodeCapacity[N]=...;  //�洢�ռ�                //node capacity

float Band[Udlink]=...; //�ߵĴ���                   //link bandwidth

float dataRate[D]=...;//data rate

tuple traffic
{
  int index;
  int edge;//������������
  float rate;//������
  float delay;//������ӳ�
  
};



{traffic} request=...;



dvar float X[request][D][N];


//A[P][D]
minimize 
			
			  sum(r in request, d in D,n in N) (X[r][d][n]*(AccessT[n][r.edge]+DataSize[d]/100)/200);	
			 //	sum(p in P, d in D,n in N) (X[p][d][n]*AccessT[n][p]);	
			 //AccessT[n][p]+Coefficient[d][n]
subject to
{
  //Placement constraint
  


forall(d in D,r in request)
 sum(n in N) X[r][d][n]==1;
 
 forall(d in D, n in N,r in request)
{ 
  X[r][d][n]<=1;
  X[r][d][n]>=0;
} 
 

forall(n in N,r in request)
 sum(p in P, d in D)(X[r][d][n]*DataSize[d])<=NodeCapacity[n];  
 
 //link bandwdith constraint 
  //forall(<m,n> in Udlink)
   //sum(r in request, d in D) X[r][d][m]*r.rate + sum(r in request, d in D)X[r][d][n]*r.rate <= Band[<m,n>];

}


     