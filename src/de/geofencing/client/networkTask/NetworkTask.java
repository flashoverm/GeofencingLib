package de.geofencing.client.networkTask;

import android.os.AsyncTask;
import de.geofencing.client.handler.LogHandler;
import de.geofencing.log.LogEntry;

/** Provides a AsyncTask which is used for network communications.
 * Occurring errors will be handled by the LogHandler.
 *  
 *  Instruction which should be executed are defined in "runInBackground".
 *  Execution is started by calling "runTask"
 *  
 *  Instructions which should be executed after successful execution
 *  can be defined by overwriting "onSuccess".
 *  
 *  Instructions which should be executed after a failure of the task
 *  can be defined by overwriting "afterFailure".
 *  
 * 
 * @author Markus Thral
 *
 * @param <Params> Type of parameter given to the task
 * @param <Progress> Type of parameter given from task on progress
 * @param <Result> Type of parameter given from the task to the onSuccess method
 */
public abstract class NetworkTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	
	private Exception exception;
	
	private LogHandler logHandler;
	
	/** Constructs a new NetworkTask
	 * 
	 */
	public NetworkTask(){
		super();
	}
	
	/** Sets given LogHandler which is be called to show the 
	 * error-message to the user and executes the task
	 * 
	 * @param logHandler Handles the occurring errors
	 * @param arg0 Arguments of the Task
	 * @return instance of AsyncTask
	 */
	public AsyncTask<Params, Progress, Result> runTask(LogHandler logHandler, @SuppressWarnings("unchecked") Params... arg0){
		this.logHandler = logHandler;
		return this.execute(arg0);
	}
	
	/** Defines instructions which should be executed. 
	 *  If finished, error handling will be done.
	 * 
	 * @param arg0 Array of Arguments of type T
	 * @return defined return-value of type V
	 * @throws Exception throws Exception if an error occurs
	 */
	protected abstract Result runInBackground(@SuppressWarnings("unchecked") Params... arg0) throws Exception;

	@Override
    protected Result doInBackground(@SuppressWarnings("unchecked") Params... params){
		try{
			return runInBackground(params);
		}
		catch(Exception exception){
			this.exception = exception;
		}
		return null;
	}
	
	@Override
    protected void onPostExecute (Result result){
		if(this.exception != null){
			if(logHandler != null){
				logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, exception));
			}
			afterFailure();
		}
		else{
			onSuccess(result);
		}
	}
	
	/** Defines what should be done if the task finishes successful
	 * 
	 * @param result Result of the task with type V
	 */
	protected void onSuccess(Result result){
		
	}
	
	/** Defines what should be done after an error occurred
	 * 
	 */
	protected void afterFailure(){
		
	}
}
