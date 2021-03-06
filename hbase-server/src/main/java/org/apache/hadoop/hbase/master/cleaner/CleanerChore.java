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
name|cleaner
package|;
end_package

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|LinkedList
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
name|concurrent
operator|.
name|CompletableFuture
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|fs
operator|.
name|PathIsNotEmptyDirectoryException
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
name|ScheduledChore
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
name|util
operator|.
name|FutureUtils
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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

begin_comment
comment|/**  * Abstract Cleaner that uses a chain of delegates to clean a directory of files  * @param<T> Cleaner delegate class that is dynamically loaded from configuration  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|CleanerChore
parameter_list|<
name|T
extends|extends
name|FileCleanerDelegate
parameter_list|>
extends|extends
name|ScheduledChore
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CleanerChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|AVAIL_PROCESSORS
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
decl_stmt|;
comment|/**    * If it is an integer and>= 1, it would be the size;    * if 0.0< size<= 1.0, size would be available processors * size.    * Pay attention that 1.0 is different from 1, former indicates it will use 100% of cores,    * while latter will use only 1 thread for chore to scan dir.    */
specifier|public
specifier|static
specifier|final
name|String
name|CHORE_POOL_SIZE
init|=
literal|"hbase.cleaner.scan.dir.concurrent.size"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DEFAULT_CHORE_POOL_SIZE
init|=
literal|"0.25"
decl_stmt|;
specifier|private
specifier|final
name|DirScanPool
name|pool
decl_stmt|;
specifier|protected
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|oldFileDir
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|enabled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|T
argument_list|>
name|cleanersChain
decl_stmt|;
specifier|public
name|CleanerChore
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|int
name|sleepPeriod
parameter_list|,
specifier|final
name|Stoppable
name|s
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|oldFileDir
parameter_list|,
name|String
name|confKey
parameter_list|,
name|DirScanPool
name|pool
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|sleepPeriod
argument_list|,
name|s
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|oldFileDir
argument_list|,
name|confKey
argument_list|,
name|pool
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param name name of the chore being run    * @param sleepPeriod the period of time to sleep between each run    * @param s the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param oldFileDir the path to the archived files    * @param confKey configuration key for the classes to instantiate    * @param pool the thread pool used to scan directories    * @param params members could be used in cleaner    */
specifier|public
name|CleanerChore
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|int
name|sleepPeriod
parameter_list|,
specifier|final
name|Stoppable
name|s
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|oldFileDir
parameter_list|,
name|String
name|confKey
parameter_list|,
name|DirScanPool
name|pool
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|s
argument_list|,
name|sleepPeriod
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|pool
argument_list|,
literal|"Chore's pool can not be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|oldFileDir
operator|=
name|oldFileDir
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
name|initCleanerChain
argument_list|(
name|confKey
argument_list|)
expr_stmt|;
block|}
comment|/**    * Calculate size for cleaner pool.    * @param poolSize size from configuration    * @return size of pool after calculation    */
specifier|static
name|int
name|calculatePoolSize
parameter_list|(
name|String
name|poolSize
parameter_list|)
block|{
if|if
condition|(
name|poolSize
operator|.
name|matches
argument_list|(
literal|"[1-9][0-9]*"
argument_list|)
condition|)
block|{
comment|// If poolSize is an integer, return it directly,
comment|// but upmost to the number of available processors.
name|int
name|size
init|=
name|Math
operator|.
name|min
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|poolSize
argument_list|)
argument_list|,
name|AVAIL_PROCESSORS
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
operator|==
name|AVAIL_PROCESSORS
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Use full core processors to scan dir, size={}"
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
elseif|else
if|if
condition|(
name|poolSize
operator|.
name|matches
argument_list|(
literal|"0.[0-9]+|1.0"
argument_list|)
condition|)
block|{
comment|// if poolSize is a double, return poolSize * availableProcessors;
comment|// Ensure that we always return at least one.
name|int
name|computedThreads
init|=
call|(
name|int
call|)
argument_list|(
name|AVAIL_PROCESSORS
operator|*
name|Double
operator|.
name|valueOf
argument_list|(
name|poolSize
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|computedThreads
operator|<
literal|1
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Computed {} threads for CleanerChore, using 1 instead"
argument_list|,
name|computedThreads
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
name|computedThreads
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unrecognized value: "
operator|+
name|poolSize
operator|+
literal|" for "
operator|+
name|CHORE_POOL_SIZE
operator|+
literal|", use default config: "
operator|+
name|DEFAULT_CHORE_POOL_SIZE
operator|+
literal|" instead."
argument_list|)
expr_stmt|;
return|return
name|calculatePoolSize
argument_list|(
name|DEFAULT_CHORE_POOL_SIZE
argument_list|)
return|;
block|}
block|}
comment|/**    * Validate the file to see if it even belongs in the directory. If it is valid, then the file    * will go through the cleaner delegates, but otherwise the file is just deleted.    * @param file full {@link Path} of the file to be checked    * @return<tt>true</tt> if the file is valid,<tt>false</tt> otherwise    */
specifier|protected
specifier|abstract
name|boolean
name|validate
parameter_list|(
name|Path
name|file
parameter_list|)
function_decl|;
comment|/**    * Instantiate and initialize all the file cleaners set in the configuration    * @param confKey key to get the file cleaner classes from the configuration    */
specifier|private
name|void
name|initCleanerChain
parameter_list|(
name|String
name|confKey
parameter_list|)
block|{
name|this
operator|.
name|cleanersChain
operator|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
expr_stmt|;
name|String
index|[]
name|logCleaners
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|confKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|logCleaners
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|className
range|:
name|logCleaners
control|)
block|{
name|T
name|logCleaner
init|=
name|newFileCleaner
argument_list|(
name|className
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|logCleaner
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialize cleaner={}"
argument_list|,
name|className
argument_list|)
expr_stmt|;
name|this
operator|.
name|cleanersChain
operator|.
name|add
argument_list|(
name|logCleaner
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * A utility method to create new instances of LogCleanerDelegate based on the class name of the    * LogCleanerDelegate.    * @param className fully qualified class name of the LogCleanerDelegate    * @param conf used configuration    * @return the new instance    */
specifier|private
name|T
name|newFileCleaner
parameter_list|(
name|String
name|className
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|FileCleanerDelegate
argument_list|>
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|FileCleanerDelegate
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|T
name|cleaner
init|=
operator|(
name|T
operator|)
name|c
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|init
argument_list|(
name|this
operator|.
name|params
argument_list|)
expr_stmt|;
return|return
name|cleaner
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can NOT create CleanerDelegate={}"
argument_list|,
name|className
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// skipping if can't instantiate
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
if|if
condition|(
name|getEnabled
argument_list|()
condition|)
block|{
try|try
block|{
name|pool
operator|.
name|latchCountUp
argument_list|()
expr_stmt|;
if|if
condition|(
name|runCleaner
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Cleaned all WALs under {}"
argument_list|,
name|oldFileDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"WALs outstanding under {}"
argument_list|,
name|oldFileDir
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|pool
operator|.
name|latchCountDown
argument_list|()
expr_stmt|;
block|}
comment|// After each cleaner chore, checks if received reconfigure notification while cleaning.
comment|// First in cleaner turns off notification, to avoid another cleaner updating pool again.
comment|// This cleaner is waiting for other cleaners finishing their jobs.
comment|// To avoid missing next chore, only wait 0.8 * period, then shutdown.
name|pool
operator|.
name|tryUpdatePoolSize
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|0.8
operator|*
name|getTimeUnit
argument_list|()
operator|.
name|toMillis
argument_list|(
name|getPeriod
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Cleaner chore disabled! Not cleaning."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|preRunCleaner
parameter_list|()
block|{
name|cleanersChain
operator|.
name|forEach
argument_list|(
name|FileCleanerDelegate
operator|::
name|preClean
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|runCleaner
parameter_list|()
block|{
name|preRunCleaner
argument_list|()
expr_stmt|;
try|try
block|{
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|pool
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
name|traverseAndDelete
argument_list|(
name|oldFileDir
argument_list|,
literal|true
argument_list|,
name|future
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|future
operator|.
name|get
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to traverse and delete the dir: {}"
argument_list|,
name|oldFileDir
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Sort the given list in (descending) order of the space each element takes    * @param dirs the list to sort, element in it should be directory (not file)    */
specifier|private
name|void
name|sortByConsumedSpace
parameter_list|(
name|List
argument_list|<
name|FileStatus
argument_list|>
name|dirs
parameter_list|)
block|{
if|if
condition|(
name|dirs
operator|==
literal|null
operator|||
name|dirs
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
comment|// no need to sort for empty or single directory
return|return;
block|}
name|dirs
operator|.
name|sort
argument_list|(
operator|new
name|Comparator
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
block|{
name|HashMap
argument_list|<
name|FileStatus
argument_list|,
name|Long
argument_list|>
name|directorySpaces
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|FileStatus
name|f1
parameter_list|,
name|FileStatus
name|f2
parameter_list|)
block|{
name|long
name|f1ConsumedSpace
init|=
name|getSpace
argument_list|(
name|f1
argument_list|)
decl_stmt|;
name|long
name|f2ConsumedSpace
init|=
name|getSpace
argument_list|(
name|f2
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|compare
argument_list|(
name|f2ConsumedSpace
argument_list|,
name|f1ConsumedSpace
argument_list|)
return|;
block|}
specifier|private
name|long
name|getSpace
parameter_list|(
name|FileStatus
name|f
parameter_list|)
block|{
name|Long
name|cached
init|=
name|directorySpaces
operator|.
name|get
argument_list|(
name|f
argument_list|)
decl_stmt|;
if|if
condition|(
name|cached
operator|!=
literal|null
condition|)
block|{
return|return
name|cached
return|;
block|}
try|try
block|{
name|long
name|space
init|=
name|f
operator|.
name|isDirectory
argument_list|()
condition|?
name|fs
operator|.
name|getContentSummary
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|getSpaceConsumed
argument_list|()
else|:
name|f
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|directorySpaces
operator|.
name|put
argument_list|(
name|f
argument_list|,
name|space
argument_list|)
expr_stmt|;
return|return
name|space
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Failed to get space consumed by path={}"
argument_list|,
name|f
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run the given files through each of the cleaners to see if it should be deleted, deleting it if    * necessary.    * @param files List of FileStatus for the files to check (and possibly delete)    * @return true iff successfully deleted all files    */
specifier|private
name|boolean
name|checkAndDeleteFiles
parameter_list|(
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
block|{
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// first check to see if the path is valid
name|List
argument_list|<
name|FileStatus
argument_list|>
name|validFiles
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|files
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|invalidFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|validate
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|validFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found a wrongly formatted file: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|+
literal|" - will delete it."
argument_list|)
expr_stmt|;
name|invalidFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|deletableValidFiles
init|=
name|validFiles
decl_stmt|;
comment|// check each of the cleaners for the valid files
for|for
control|(
name|T
name|cleaner
range|:
name|cleanersChain
control|)
block|{
if|if
condition|(
name|cleaner
operator|.
name|isStopped
argument_list|()
operator|||
name|this
operator|.
name|getStopper
argument_list|()
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"A file cleaner"
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|" is stopped, won't delete any more files in:"
operator|+
name|this
operator|.
name|oldFileDir
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|filteredFiles
init|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|deletableValidFiles
argument_list|)
decl_stmt|;
comment|// trace which cleaner is holding on to each file
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|ImmutableSet
argument_list|<
name|FileStatus
argument_list|>
name|filteredFileSet
init|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|filteredFiles
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|deletableValidFiles
control|)
block|{
if|if
condition|(
operator|!
name|filteredFileSet
operator|.
name|contains
argument_list|(
name|file
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|+
literal|" is not deletable according to:"
operator|+
name|cleaner
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|deletableValidFiles
operator|=
name|filteredFiles
expr_stmt|;
block|}
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|filesToDelete
init|=
name|Iterables
operator|.
name|concat
argument_list|(
name|invalidFiles
argument_list|,
name|deletableValidFiles
argument_list|)
decl_stmt|;
return|return
name|deleteFiles
argument_list|(
name|filesToDelete
argument_list|)
operator|==
name|files
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Check if a empty directory with no subdirs or subfiles can be deleted    * @param dir Path of the directory    * @return True if the directory can be deleted, otherwise false    */
specifier|private
name|boolean
name|isEmptyDirDeletable
parameter_list|(
name|Path
name|dir
parameter_list|)
block|{
for|for
control|(
name|T
name|cleaner
range|:
name|cleanersChain
control|)
block|{
if|if
condition|(
name|cleaner
operator|.
name|isStopped
argument_list|()
operator|||
name|this
operator|.
name|getStopper
argument_list|()
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"A file cleaner {} is stopped, won't delete the empty directory {}"
argument_list|,
name|this
operator|.
name|getName
argument_list|()
argument_list|,
name|dir
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|cleaner
operator|.
name|isEmptyDirDeletable
argument_list|(
name|dir
argument_list|)
condition|)
block|{
comment|// If one of the cleaner need the empty directory, skip delete it
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Delete the given files    * @param filesToDelete files to delete    * @return number of deleted files    */
specifier|protected
name|int
name|deleteFiles
parameter_list|(
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|filesToDelete
parameter_list|)
block|{
name|int
name|deletedFileCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|filesToDelete
control|)
block|{
name|Path
name|filePath
init|=
name|file
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"Removing {} from archive"
argument_list|,
name|filePath
argument_list|)
expr_stmt|;
try|try
block|{
name|boolean
name|success
init|=
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|filePath
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|success
condition|)
block|{
name|deletedFileCount
operator|++
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempted to delete:"
operator|+
name|filePath
operator|+
literal|", but couldn't. Run cleaner chain and attempt to delete on next pass."
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|e
operator|instanceof
name|RemoteException
condition|?
operator|(
operator|(
name|RemoteException
operator|)
name|e
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
else|:
name|e
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error while deleting: "
operator|+
name|filePath
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|deletedFileCount
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|cleanup
parameter_list|()
block|{
for|for
control|(
name|T
name|lc
range|:
name|this
operator|.
name|cleanersChain
control|)
block|{
try|try
block|{
name|lc
operator|.
name|stop
argument_list|(
literal|"Exiting"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Stopping"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
name|int
name|getChorePoolSize
parameter_list|()
block|{
return|return
name|pool
operator|.
name|getSize
argument_list|()
return|;
block|}
comment|/**    * @param enabled    */
specifier|public
name|boolean
name|setEnabled
parameter_list|(
specifier|final
name|boolean
name|enabled
parameter_list|)
block|{
return|return
name|this
operator|.
name|enabled
operator|.
name|getAndSet
argument_list|(
name|enabled
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|getEnabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|enabled
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
interface|interface
name|Action
parameter_list|<
name|T
parameter_list|>
block|{
name|T
name|act
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
comment|/**    * Attempts to clean up a directory(its subdirectories, and files) in a    * {@link java.util.concurrent.ThreadPoolExecutor} concurrently. We can get the final result by    * calling result.get().    */
specifier|private
name|void
name|traverseAndDelete
parameter_list|(
name|Path
name|dir
parameter_list|,
name|boolean
name|root
parameter_list|,
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|result
parameter_list|)
block|{
try|try
block|{
comment|// Step.1: List all files under the given directory.
name|List
argument_list|<
name|FileStatus
argument_list|>
name|allPaths
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|fs
operator|.
name|listStatus
argument_list|(
name|dir
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|subDirs
init|=
name|allPaths
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|FileStatus
operator|::
name|isDirectory
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
init|=
name|allPaths
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|FileStatus
operator|::
name|isFile
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
comment|// Step.2: Try to delete all the deletable files.
name|boolean
name|allFilesDeleted
init|=
name|files
operator|.
name|isEmpty
argument_list|()
operator|||
name|deleteAction
argument_list|(
parameter_list|()
lambda|->
name|checkAndDeleteFiles
argument_list|(
name|files
argument_list|)
argument_list|,
literal|"files"
argument_list|,
name|dir
argument_list|)
decl_stmt|;
comment|// Step.3: Start to traverse and delete the sub-directories.
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|subDirs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sortByConsumedSpace
argument_list|(
name|subDirs
argument_list|)
expr_stmt|;
comment|// Submit the request of sub-directory deletion.
name|subDirs
operator|.
name|forEach
argument_list|(
name|subDir
lambda|->
block|{
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|subFuture
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|pool
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
name|traverseAndDelete
argument_list|(
name|subDir
operator|.
name|getPath
argument_list|()
argument_list|,
literal|false
argument_list|,
name|subFuture
argument_list|)
argument_list|)
expr_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|subFuture
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
comment|// Step.4: Once all sub-files& sub-directories are deleted, then can try to delete the
comment|// current directory asynchronously.
name|FutureUtils
operator|.
name|addListener
argument_list|(
name|CompletableFuture
operator|.
name|allOf
argument_list|(
name|futures
operator|.
name|toArray
argument_list|(
operator|new
name|CompletableFuture
index|[
name|futures
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|,
parameter_list|(
name|voidObj
parameter_list|,
name|e
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|boolean
name|allSubDirsDeleted
init|=
name|futures
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|CompletableFuture
operator|::
name|join
argument_list|)
decl_stmt|;
name|boolean
name|deleted
init|=
name|allFilesDeleted
operator|&&
name|allSubDirsDeleted
operator|&&
name|isEmptyDirDeletable
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|deleted
operator|&&
operator|!
name|root
condition|)
block|{
comment|// If and only if files and sub-dirs under current dir are deleted successfully, and
comment|// the empty directory can be deleted, and it is not the root dir then task will
comment|// try to delete it.
name|deleted
operator|=
name|deleteAction
argument_list|(
parameter_list|()
lambda|->
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|"dir"
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|complete
argument_list|(
name|deleted
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ie
parameter_list|)
block|{
comment|// Must handle the inner exception here, otherwise the result may get stuck if one
comment|// sub-directory get some failure.
name|result
operator|.
name|completeExceptionally
argument_list|(
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to traverse and delete the path: {}"
argument_list|,
name|dir
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|result
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Perform a delete on a specified type.    * @param deletion a delete    * @param type possible values are 'files', 'subdirs', 'dirs'    * @return true if it deleted successfully, false otherwise    */
specifier|private
name|boolean
name|deleteAction
parameter_list|(
name|Action
argument_list|<
name|Boolean
argument_list|>
name|deletion
parameter_list|,
name|String
name|type
parameter_list|,
name|Path
name|dir
parameter_list|)
block|{
name|boolean
name|deleted
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Start deleting {} under {}"
argument_list|,
name|type
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|deleted
operator|=
name|deletion
operator|.
name|act
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|PathIsNotEmptyDirectoryException
name|exception
parameter_list|)
block|{
comment|// N.B. HDFS throws this exception when we try to delete a non-empty directory, but
comment|// LocalFileSystem throws a bare IOException. So some test code will get the verbose
comment|// message below.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Couldn't delete '{}' yet because it isn't empty w/exception."
argument_list|,
name|dir
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|deleted
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Could not delete {} under {}. might be transient; we'll retry. if it keeps "
operator|+
literal|"happening, use following exception when asking on mailing list."
argument_list|,
name|type
argument_list|,
name|dir
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|deleted
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"unexpected exception: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|deleted
operator|=
literal|false
expr_stmt|;
block|}
name|LOG
operator|.
name|trace
argument_list|(
literal|"Finish deleting {} under {}, deleted="
argument_list|,
name|type
argument_list|,
name|dir
argument_list|,
name|deleted
argument_list|)
expr_stmt|;
return|return
name|deleted
return|;
block|}
block|}
end_class

end_unit

