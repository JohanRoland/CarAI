package prediction;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.error.ErrorFunction;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

public class Network {
		public static double XOR_INPUT [ ] [ ]  ={ {0.0 ,  0.0},{1.0, 0.0},{0.0,1.0},{1.0, 1.0}};
		/*
		The  i d e a l  data  necessary  for XOR.
		*/
		public static double XOR_IDEAL [ ] [ ]  ={ {0.0},{1.0},{1.0},{0.0}};
		
		public Network(){
			//  create  a neural  network ,  without  using a  factory
			BasicNetwork  network =	new	BasicNetwork();
			network.addLayer(new BasicLayer (null, true, 2));
			network.addLayer(new BasicLayer (new ActivationSigmoid(), true, 3));
			network.addLayer(new BasicLayer(new	ActivationSigmoid (), false, 1));
			network.getStructure().finalizeStructure() ;
			network.reset();
			//  create  training  data
			MLDataSet  trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL) ;
			//  train  the  neural  network
			final ResilientPropagation  train =	new	ResilientPropagation ( network ,  trainingSet ) ;
			
			customErrorFunction ef = new customErrorFunction();
			train.setErrorFunction(ef);
			int	epoch = 1;
			do
			{
				train.iteration();
				System.out.println ("Epoch #" + epoch + " Error : " + train.getError());
				epoch++;
			}
			while( train.getError()	>0.01);
			
			train.finishTraining();
			//  t e s t  the  neural  network
			System.out.println( " Neural  Network  Results : " ) ;
			for(MLDataPair  pair :  trainingSet  )
			{
				final MLData output = network . compute ( pair . getInput () ) ;
				System.out.println( pair.getInput ().getData(0) + " , " + pair.getInput().getData(1)+ " ,  actual=" + output.getData(0) + " , ideal="
						+ pair.getIdeal().getData(0)) ;
			}
		
		}
		
		class customErrorFunction implements ErrorFunction 
		{
			customErrorFunction()
			{}
			@Override
			public void calculateError(double[] ideal, double[] actual, double[] error)
			{
				for(int i=0; i<ideal.length;i++)
					error[i]=ideal[i]-actual[i];
				
			}
			
		}
}
