package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.data.Util;
import org.theronin.expensetracker.data.backend.RemoteSync;
import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import timber.log.Timber;

public class FakeRemoteSync extends RemoteSync {

    private boolean alwaysPass;

    private boolean calledRemoteSave;
    private boolean calledRemoteDelete;

    public FakeRemoteSync(boolean alwaysPass) {
        this.alwaysPass = alwaysPass;
    }

    public boolean calledRemoteSave() {
        return calledRemoteSave;
    }

    public boolean calledRemoteDelete() {
        return calledRemoteDelete;
    }

    @Override
    protected void bulkAddOperation(List<? extends Entity> entities) throws Exception {
        calledRemoteSave = true;
        syncOp(entities);
    }

    private void syncOp(List<? extends Entity> entities) throws Exception {
        if (alwaysPass) {
            Timber.v("Test sync : " + entities.size() + " entities synced with the 'backend'");
        } else {
            throw new Exception("Syncing with 'backend' failed");
        }
    }

    @Override
    protected String getObjectId(Entity entity) {
        return Util.generateRandomGlobalId();
    }

    @Override
    protected void bulkDeleteOperation(List<? extends Entity> entities) throws Exception {
        calledRemoteDelete = true;
        syncOp(entities);
    }

    @Override
    protected void registerForPush() {

    }

    @Override
    protected List<Entry> findOperation(long lastSync) throws Exception {
        return null;
    }
}
