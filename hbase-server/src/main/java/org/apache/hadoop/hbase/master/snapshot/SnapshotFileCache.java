begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|master
operator|.
name|snapshot
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Timer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimerTask
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Stoppable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|CorruptedSnapshotException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|SnapshotDescriptionUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|FSUtils
import|;
end_import

begin_comment
comment|/**  * Intelligently keep track of all the files for all the snapshots.  *<p>  * A cache of files is kept to avoid querying the {@link FileSystem} frequently. If there is a cache  * miss the directory modification time is used to ensure that we don't rescan directories that we  * already have in cache. We only check the modification times of the snapshot directories  * (/hbase/.snapshot/[snapshot_name]) to determine if the files need to be loaded into the cache.  *<p>  * New snapshots will be added to the cache and deleted snapshots will be removed when we refresh  * the cache. If the files underneath a snapshot directory are changed, but not the snapshot itself,  * we will ignore updates to that snapshot's files.  *<p>  * This is sufficient because each snapshot has its own directory and is added via an atomic rename  *<i>once</i>, when the snapshot is created. We don't need to worry about the data in the snapshot  * being run.  *<p>  * Further, the cache is periodically refreshed ensure that files in snapshots that were deleted are  * also removed from the cache.  *<p>  * A {@link SnapshotFileCache.SnapshotFileInspector} must be passed when creating<tt>this</tt> to  * allow extraction of files under /hbase/.snapshot/[snapshot name] directory, for each snapshot.  * This allows you to only cache files under, for instance, all the logs in the .logs directory or  * all the files under all the regions.  *<p>  *<tt>this</tt> also considers all running snapshots (those under /hbase/.snapshot/.tmp) as valid  * snapshots and will attempt to cache files from those snapshots as well.  *<p>  * Queries about a given file are thread-safe with respect to multiple queries and cache refreshes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|SnapshotFileCache
implements|implements
name|Stoppable
block|{
interface|interface
name|SnapshotFileInspector
block|{
comment|/**      * Returns a collection of file names needed by the snapshot.      * @param snapshotDir {@link Path} to the snapshot directory to scan.      * @return the collection of file names needed by the snapshot.      */
name|Collection
argument_list|<
name|String
argument_list|>
name|filesUnderSnapshot
parameter_list|(
specifier|final
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SnapshotFileCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|stop
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|SnapshotFileInspector
name|fileInspector
decl_stmt|;
specifier|private
specifier|final
name|Path
name|snapshotDir
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|cache
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * This is a helper map of information about the snapshot directories so we don't need to rescan    * them if they haven't changed since the last time we looked.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SnapshotDirectoryInfo
argument_list|>
name|snapshots
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SnapshotDirectoryInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Timer
name|refreshTimer
decl_stmt|;
specifier|private
name|long
name|lastModifiedTime
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
comment|/**    * Create a snapshot file cache for all snapshots under the specified [root]/.snapshot on the    * filesystem.    *<p>    * Immediately loads the file cache.    * @param conf to extract the configured {@link FileSystem} where the snapshots are stored and    *          hbase root directory    * @param cacheRefreshPeriod frequency (ms) with which the cache should be refreshed    * @param refreshThreadName name of the cache refresh thread    * @param inspectSnapshotFiles Filter to apply to each snapshot to extract the files.    * @throws IOException if the {@link FileSystem} or root directory cannot be loaded    */
specifier|public
name|SnapshotFileCache
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|long
name|cacheRefreshPeriod
parameter_list|,
name|String
name|refreshThreadName
parameter_list|,
name|SnapshotFileInspector
name|inspectSnapshotFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|FSUtils
operator|.
name|getCurrentFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|0
argument_list|,
name|cacheRefreshPeriod
argument_list|,
name|refreshThreadName
argument_list|,
name|inspectSnapshotFiles
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a snapshot file cache for all snapshots under the specified [root]/.snapshot on the    * filesystem    * @param fs {@link FileSystem} where the snapshots are stored    * @param rootDir hbase root directory    * @param cacheRefreshPeriod period (ms) with which the cache should be refreshed    * @param cacheRefreshDelay amount of time to wait for the cache to be refreshed    * @param refreshThreadName name of the cache refresh thread    * @param inspectSnapshotFiles Filter to apply to each snapshot to extract the files.    */
specifier|public
name|SnapshotFileCache
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|long
name|cacheRefreshPeriod
parameter_list|,
name|long
name|cacheRefreshDelay
parameter_list|,
name|String
name|refreshThreadName
parameter_list|,
name|SnapshotFileInspector
name|inspectSnapshotFiles
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|fileInspector
operator|=
name|inspectSnapshotFiles
expr_stmt|;
name|this
operator|.
name|snapshotDir
operator|=
name|SnapshotDescriptionUtils
operator|.
name|getSnapshotsDir
argument_list|(
name|rootDir
argument_list|)
expr_stmt|;
comment|// periodically refresh the file cache to make sure we aren't superfluously saving files.
name|this
operator|.
name|refreshTimer
operator|=
operator|new
name|Timer
argument_list|(
name|refreshThreadName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|refreshTimer
operator|.
name|scheduleAtFixedRate
argument_list|(
operator|new
name|RefreshCacheTask
argument_list|()
argument_list|,
name|cacheRefreshDelay
argument_list|,
name|cacheRefreshPeriod
argument_list|)
expr_stmt|;
block|}
comment|/**    * Trigger a cache refresh, even if its before the next cache refresh. Does not affect pending    * cache refreshes.    *<p>    * Blocks until the cache is refreshed.    *<p>    * Exposed for TESTING.    */
specifier|public
name|void
name|triggerCacheRefreshForTesting
parameter_list|()
block|{
try|try
block|{
name|SnapshotFileCache
operator|.
name|this
operator|.
name|refreshCache
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to refresh snapshot hfile cache!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current cache:"
operator|+
name|cache
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check to see if any of the passed file names is contained in any of the snapshots.    * First checks an in-memory cache of the files to keep. If its not in the cache, then the cache    * is refreshed and the cache checked again for that file.    * This ensures that we never return files that exist.    *<p>    * Note this may lead to periodic false positives for the file being referenced. Periodically, the    * cache is refreshed even if there are no requests to ensure that the false negatives get removed    * eventually. For instance, suppose you have a file in the snapshot and it gets loaded into the    * cache. Then at some point later that snapshot is deleted. If the cache has not been refreshed    * at that point, cache will still think the file system contains that file and return    *<tt>true</tt>, even if it is no longer present (false positive). However, if the file never was    * on the filesystem, we will never find it and always return<tt>false</tt>.    * @param files file to check, NOTE: Relies that files are loaded from hdfs before method    *              is called (NOT LAZY)    * @return<tt>unReferencedFiles</tt> the collection of files that do not have snapshot references    * @throws IOException if there is an unexpected error reaching the filesystem.    */
comment|// XXX this is inefficient to synchronize on the method, when what we really need to guard against
comment|// is an illegal access to the cache. Really we could do a mutex-guarded pointer swap on the
comment|// cache, but that seems overkill at the moment and isn't necessarily a bottleneck.
specifier|public
specifier|synchronized
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|getUnreferencedFiles
parameter_list|(
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|,
specifier|final
name|SnapshotManager
name|snapshotManager
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|FileStatus
argument_list|>
name|unReferencedFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|snapshotsInProgress
init|=
literal|null
decl_stmt|;
name|boolean
name|refreshed
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|String
name|fileName
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|refreshed
operator|&&
operator|!
name|cache
operator|.
name|contains
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
name|refreshCache
argument_list|()
expr_stmt|;
name|refreshed
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|cache
operator|.
name|contains
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|snapshotsInProgress
operator|==
literal|null
condition|)
block|{
name|snapshotsInProgress
operator|=
name|getSnapshotsInProgress
argument_list|(
name|snapshotManager
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|snapshotsInProgress
operator|.
name|contains
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|unReferencedFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
return|return
name|unReferencedFiles
return|;
block|}
specifier|private
specifier|synchronized
name|void
name|refreshCache
parameter_list|()
throws|throws
name|IOException
block|{
comment|// get the status of the snapshots directory and check if it is has changes
name|FileStatus
name|dirStatus
decl_stmt|;
try|try
block|{
name|dirStatus
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|snapshotDir
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|cache
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Snapshot directory: "
operator|+
name|snapshotDir
operator|+
literal|" doesn't exist"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// if the snapshot directory wasn't modified since we last check, we are done
if|if
condition|(
name|dirStatus
operator|.
name|getModificationTime
argument_list|()
operator|<=
name|this
operator|.
name|lastModifiedTime
condition|)
return|return;
comment|// directory was modified, so we need to reload our cache
comment|// there could be a slight race here where we miss the cache, check the directory modification
comment|// time, then someone updates the directory, causing us to not scan the directory again.
comment|// However, snapshot directories are only created once, so this isn't an issue.
comment|// 1. update the modified time
name|this
operator|.
name|lastModifiedTime
operator|=
name|dirStatus
operator|.
name|getModificationTime
argument_list|()
expr_stmt|;
comment|// 2.clear the cache
name|this
operator|.
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SnapshotDirectoryInfo
argument_list|>
name|known
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SnapshotDirectoryInfo
argument_list|>
argument_list|()
decl_stmt|;
comment|// 3. check each of the snapshot directories
name|FileStatus
index|[]
name|snapshots
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshots
operator|==
literal|null
condition|)
block|{
comment|// remove all the remembered snapshots because we don't have any left
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
name|this
operator|.
name|snapshots
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No snapshots on-disk, cache empty"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|snapshots
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// 3.1 iterate through the on-disk snapshots
for|for
control|(
name|FileStatus
name|snapshot
range|:
name|snapshots
control|)
block|{
name|String
name|name
init|=
name|snapshot
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// its not the tmp dir,
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|SnapshotDescriptionUtils
operator|.
name|SNAPSHOT_TMP_DIR_NAME
argument_list|)
condition|)
block|{
name|SnapshotDirectoryInfo
name|files
init|=
name|this
operator|.
name|snapshots
operator|.
name|remove
argument_list|(
name|name
argument_list|)
decl_stmt|;
comment|// 3.1.1 if we don't know about the snapshot or its been modified, we need to update the
comment|// files the latter could occur where I create a snapshot, then delete it, and then make a
comment|// new snapshot with the same name. We will need to update the cache the information from
comment|// that new snapshot, even though it has the same name as the files referenced have
comment|// probably changed.
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|files
operator|.
name|hasBeenModified
argument_list|(
name|snapshot
operator|.
name|getModificationTime
argument_list|()
argument_list|)
condition|)
block|{
comment|// get all files for the snapshot and create a new info
name|Collection
argument_list|<
name|String
argument_list|>
name|storedFiles
init|=
name|fileInspector
operator|.
name|filesUnderSnapshot
argument_list|(
name|snapshot
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|files
operator|=
operator|new
name|SnapshotDirectoryInfo
argument_list|(
name|snapshot
operator|.
name|getModificationTime
argument_list|()
argument_list|,
name|storedFiles
argument_list|)
expr_stmt|;
block|}
comment|// 3.2 add all the files to cache
name|this
operator|.
name|cache
operator|.
name|addAll
argument_list|(
name|files
operator|.
name|getFiles
argument_list|()
argument_list|)
expr_stmt|;
name|known
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|files
argument_list|)
expr_stmt|;
block|}
block|}
comment|// 4. set the snapshots we are tracking
name|this
operator|.
name|snapshots
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|snapshots
operator|.
name|putAll
argument_list|(
name|known
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|List
argument_list|<
name|String
argument_list|>
name|getSnapshotsInProgress
parameter_list|(
specifier|final
name|SnapshotManager
name|snapshotManager
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|snapshotInProgress
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|// only add those files to the cache, but not to the known snapshots
name|Path
name|snapshotTmpDir
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|SnapshotDescriptionUtils
operator|.
name|SNAPSHOT_TMP_DIR_NAME
argument_list|)
decl_stmt|;
comment|// only add those files to the cache, but not to the known snapshots
name|FileStatus
index|[]
name|running
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|snapshotTmpDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|running
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileStatus
name|run
range|:
name|running
control|)
block|{
name|ReentrantLock
name|lock
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|snapshotManager
operator|!=
literal|null
condition|)
block|{
name|lock
operator|=
name|snapshotManager
operator|.
name|getLocks
argument_list|()
operator|.
name|acquireLock
argument_list|(
name|run
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|snapshotInProgress
operator|.
name|addAll
argument_list|(
name|fileInspector
operator|.
name|filesUnderSnapshot
argument_list|(
name|run
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CorruptedSnapshotException
name|e
parameter_list|)
block|{
comment|// See HBASE-16464
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|FileNotFoundException
condition|)
block|{
comment|// If the snapshot is corrupt, we will delete it
name|fs
operator|.
name|delete
argument_list|(
name|run
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"delete the "
operator|+
name|run
operator|.
name|getPath
argument_list|()
operator|+
literal|" due to exception:"
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|lock
operator|!=
literal|null
condition|)
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|snapshotInProgress
return|;
block|}
comment|/**    * Simple helper task that just periodically attempts to refresh the cache    */
specifier|public
class|class
name|RefreshCacheTask
extends|extends
name|TimerTask
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|SnapshotFileCache
operator|.
name|this
operator|.
name|refreshCache
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to refresh snapshot hfile cache!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|stop
condition|)
block|{
name|this
operator|.
name|stop
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|refreshTimer
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|stop
return|;
block|}
comment|/**    * Information about a snapshot directory    */
specifier|private
specifier|static
class|class
name|SnapshotDirectoryInfo
block|{
name|long
name|lastModified
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|files
decl_stmt|;
specifier|public
name|SnapshotDirectoryInfo
parameter_list|(
name|long
name|mtime
parameter_list|,
name|Collection
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
block|{
name|this
operator|.
name|lastModified
operator|=
name|mtime
expr_stmt|;
name|this
operator|.
name|files
operator|=
name|files
expr_stmt|;
block|}
comment|/**      * @return the hfiles in the snapshot when<tt>this</tt> was made.      */
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getFiles
parameter_list|()
block|{
return|return
name|this
operator|.
name|files
return|;
block|}
comment|/**      * Check if the snapshot directory has been modified      * @param mtime current modification time of the directory      * @return<tt>true</tt> if it the modification time of the directory is newer time when we      *         created<tt>this</tt>      */
specifier|public
name|boolean
name|hasBeenModified
parameter_list|(
name|long
name|mtime
parameter_list|)
block|{
return|return
name|this
operator|.
name|lastModified
operator|<
name|mtime
return|;
block|}
block|}
block|}
end_class

end_unit

