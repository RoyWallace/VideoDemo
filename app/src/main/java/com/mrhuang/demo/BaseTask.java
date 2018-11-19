package com.mrhuang.demo;

import android.os.AsyncTask;

public abstract class BaseTask<B, U, P> extends AsyncTask<B, U, P> {

    protected ProgressListener progressListener;

    protected CompleteListener completeListener;

    @Override

    protected void onProgressUpdate(U... values) {
        if (progressListener != null) {
            progressListener.onProgressUpdate(values);
        }
    }

    @Override
    protected void onPostExecute(P p) {
        if (completeListener != null) {
            completeListener.onComplete(p);
        }
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void setCompleteListener(CompleteListener completeListener) {
        this.completeListener = completeListener;
    }

    public interface ProgressListener<U> {
        void onProgressUpdate(U... value);
    }

    public interface CompleteListener<P> {
        void onComplete(P p);
    }
}
