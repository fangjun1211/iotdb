/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.engine.merge;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.apache.iotdb.db.engine.merge.inplace.selector.InplaceMaxFileSelector;
import org.apache.iotdb.db.engine.merge.inplace.selector.InplaceMaxSeriesMergeFileSelector;
import org.apache.iotdb.db.engine.merge.inplace.task.InplaceMergeTask;
import org.apache.iotdb.db.engine.merge.manage.MergeResource;
import org.apache.iotdb.db.engine.merge.squeeze.selector.SqueezeMaxFileSelector;
import org.apache.iotdb.db.engine.merge.squeeze.selector.SqueezeMaxSeriesMergeFileSelector;
import org.apache.iotdb.db.engine.merge.squeeze.task.SqueezeMergeTask;
import org.apache.iotdb.db.engine.storagegroup.TsFileResource;

public enum MergeFileStrategy {
  INPLACE_MAX_SERIES_NUM,
  INPLACE_MAX_FILE_NUM,
  SQUEEZE_MAX_SERIES_NUM,
  SQUEEZE_MAX_FILE_NUM;
  // TODO new strategies?

  public IMergeFileSelector getFileSelector(Collection<TsFileResource> seqFiles,
      Collection<TsFileResource> unseqFiles, long budget, long timeLowerBound) {
    switch (this) {
      case INPLACE_MAX_FILE_NUM:
        return new InplaceMaxFileSelector(seqFiles, unseqFiles, budget, timeLowerBound);
      case INPLACE_MAX_SERIES_NUM:
        return new InplaceMaxSeriesMergeFileSelector(seqFiles, unseqFiles, budget, timeLowerBound);
      case SQUEEZE_MAX_FILE_NUM:
        return new SqueezeMaxFileSelector(seqFiles, unseqFiles, budget, timeLowerBound);
      case SQUEEZE_MAX_SERIES_NUM:
        return new SqueezeMaxSeriesMergeFileSelector(seqFiles, unseqFiles, budget, timeLowerBound);
    }
    return null;
  }

  public Callable<Void> getMergeTask(MergeResource mergeResource, String storageGroupSysDir,
      MergeCallback callback,
      String taskName, int concurrentMergeSeriesNum, String storageGroupName,
      boolean isFullMerge) {
    switch (this) {
      case INPLACE_MAX_SERIES_NUM:
      case INPLACE_MAX_FILE_NUM:
        return new InplaceMergeTask(mergeResource, storageGroupSysDir, callback, taskName,
            isFullMerge, concurrentMergeSeriesNum, storageGroupName);
      case SQUEEZE_MAX_FILE_NUM:
      case SQUEEZE_MAX_SERIES_NUM:
        return new SqueezeMergeTask(mergeResource, storageGroupSysDir, callback, taskName,
            concurrentMergeSeriesNum, storageGroupName);
    }
    return null;
  }
}
