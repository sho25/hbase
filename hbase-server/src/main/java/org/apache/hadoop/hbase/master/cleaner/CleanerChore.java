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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|conf
operator|.
name|ConfigurationObserver
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
name|Predicate
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
name|ExecutionException
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
name|ForkJoinPool
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
name|RecursiveTask
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
name|FSUtils
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

begin_comment
comment|/**  * Abstract Cleaner that uses a chain of delegates to clean a directory of files  * @param<T> Cleaner delegate class that is dynamically loaded from configuration  */
end_comment

begin_class
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
implements|implements
name|ConfigurationObserver
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
operator|.
name|getName
argument_list|()
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
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_CHORE_POOL_SIZE
init|=
literal|"0.5"
decl_stmt|;
comment|// It may be waste resources for each cleaner chore own its pool,
comment|// so let's make pool for all cleaner chores.
specifier|private
specifier|static
specifier|volatile
name|ForkJoinPool
name|chorePool
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|int
name|chorePoolSize
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
specifier|private
specifier|final
name|AtomicBoolean
name|reconfig
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
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
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param name name of the chore being run    * @param sleepPeriod the period of time to sleep between each run    * @param s the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param oldFileDir the path to the archived files    * @param confKey configuration key for the classes to instantiate    * @param params members could be used in cleaner    */
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
if|if
condition|(
name|chorePool
operator|==
literal|null
condition|)
block|{
name|String
name|poolSize
init|=
name|conf
operator|.
name|get
argument_list|(
name|CHORE_POOL_SIZE
argument_list|,
name|DEFAULT_CHORE_POOL_SIZE
argument_list|)
decl_stmt|;
name|chorePoolSize
operator|=
name|calculatePoolSize
argument_list|(
name|poolSize
argument_list|)
expr_stmt|;
comment|// poolSize may be 0 or 0.0 from a careless configuration,
comment|// double check to make sure.
name|chorePoolSize
operator|=
name|chorePoolSize
operator|==
literal|0
condition|?
name|calculatePoolSize
argument_list|(
name|DEFAULT_CHORE_POOL_SIZE
argument_list|)
else|:
name|chorePoolSize
expr_stmt|;
name|this
operator|.
name|chorePool
operator|=
operator|new
name|ForkJoinPool
argument_list|(
name|chorePoolSize
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaner pool size is "
operator|+
name|chorePoolSize
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Calculate size for cleaner pool.    * @param poolSize size from configuration    * @return size of pool after calculation    */
specifier|private
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
name|valueOf
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
operator|+
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
return|return
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
name|debug
argument_list|(
literal|"Initialize cleaner="
operator|+
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
annotation|@
name|Override
specifier|public
name|void
name|onConfigurationChange
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|int
name|updatedSize
init|=
name|calculatePoolSize
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|CHORE_POOL_SIZE
argument_list|,
name|DEFAULT_CHORE_POOL_SIZE
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|updatedSize
operator|==
name|chorePoolSize
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Size from configuration is same as previous={}, no need to update."
argument_list|,
name|updatedSize
argument_list|)
expr_stmt|;
return|return;
block|}
name|chorePoolSize
operator|=
name|updatedSize
expr_stmt|;
if|if
condition|(
name|chorePool
operator|.
name|getPoolSize
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Chore does not work now, update it directly.
name|updateChorePoolSize
argument_list|(
name|updatedSize
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Chore is working, update it after chore finished.
name|reconfig
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|updateChorePoolSize
parameter_list|(
name|int
name|updatedSize
parameter_list|)
block|{
name|chorePool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Update chore's pool size from {} to {}"
argument_list|,
name|chorePool
operator|.
name|getParallelism
argument_list|()
argument_list|,
name|updatedSize
argument_list|)
expr_stmt|;
name|chorePool
operator|=
operator|new
name|ForkJoinPool
argument_list|(
name|updatedSize
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|runCleaner
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cleaned old files/dirs under {} successfully"
argument_list|,
name|oldFileDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to fully clean old files/dirs under "
operator|+
name|oldFileDir
operator|+
literal|"."
argument_list|)
expr_stmt|;
block|}
comment|// After each clean chore, checks if receives reconfigure notification while cleaning
if|if
condition|(
name|reconfig
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|updateChorePoolSize
argument_list|(
name|chorePoolSize
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
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
name|Boolean
name|runCleaner
parameter_list|()
block|{
name|preRunCleaner
argument_list|()
expr_stmt|;
name|CleanerTask
name|task
init|=
operator|new
name|CleanerTask
argument_list|(
name|this
operator|.
name|oldFileDir
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|chorePool
operator|.
name|submit
argument_list|(
name|task
argument_list|)
expr_stmt|;
return|return
name|task
operator|.
name|join
argument_list|()
return|;
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
operator|.
name|getPath
argument_list|()
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
name|chorePoolSize
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
name|IOException
function_decl|;
block|}
specifier|private
class|class
name|CleanerTask
extends|extends
name|RecursiveTask
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|private
specifier|final
name|Path
name|dir
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|root
decl_stmt|;
name|CleanerTask
parameter_list|(
specifier|final
name|FileStatus
name|dir
parameter_list|,
specifier|final
name|boolean
name|root
parameter_list|)
block|{
name|this
argument_list|(
name|dir
operator|.
name|getPath
argument_list|()
argument_list|,
name|root
argument_list|)
expr_stmt|;
block|}
name|CleanerTask
parameter_list|(
specifier|final
name|Path
name|dir
parameter_list|,
specifier|final
name|boolean
name|root
parameter_list|)
block|{
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
name|this
operator|.
name|root
operator|=
name|root
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Boolean
name|compute
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"CleanerTask "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
operator|+
literal|" starts cleaning dirs and files under "
operator|+
name|dir
operator|+
literal|" and itself."
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|FileStatus
argument_list|>
name|subDirs
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
decl_stmt|;
try|try
block|{
name|subDirs
operator|=
name|getFilteredStatus
argument_list|(
name|status
lambda|->
name|status
operator|.
name|isDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|files
operator|=
name|getFilteredStatus
argument_list|(
name|status
lambda|->
name|status
operator|.
name|isFile
argument_list|()
argument_list|)
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
name|warn
argument_list|(
name|dir
operator|+
literal|" doesn't exist, just skip it. "
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|boolean
name|nullSubDirs
init|=
name|subDirs
operator|==
literal|null
decl_stmt|;
if|if
condition|(
name|nullSubDirs
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"There is no subdir under {}"
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"There is no file under {}"
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
name|int
name|capacity
init|=
name|nullSubDirs
condition|?
literal|0
else|:
name|subDirs
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|CleanerTask
argument_list|>
name|tasks
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|capacity
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|nullSubDirs
condition|)
block|{
name|sortByConsumedSpace
argument_list|(
name|subDirs
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|subdir
range|:
name|subDirs
control|)
block|{
name|CleanerTask
name|task
init|=
operator|new
name|CleanerTask
argument_list|(
name|subdir
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|task
operator|.
name|fork
argument_list|()
expr_stmt|;
block|}
block|}
name|boolean
name|result
init|=
literal|true
decl_stmt|;
name|result
operator|&=
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
argument_list|)
expr_stmt|;
name|result
operator|&=
name|deleteAction
argument_list|(
parameter_list|()
lambda|->
name|getCleanResult
argument_list|(
name|tasks
argument_list|)
argument_list|,
literal|"subdirs"
argument_list|)
expr_stmt|;
comment|// if and only if files and subdirs under current dir are deleted successfully, and
comment|// it is not the root dir, then task will try to delete it.
if|if
condition|(
name|result
operator|&&
operator|!
name|root
condition|)
block|{
name|result
operator|&=
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
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**      * Get FileStatus with filter.      * Pay attention that FSUtils #listStatusWithStatusFilter would return null,      * even though status is empty but not null.      * @param function a filter function      * @return filtered FileStatus      * @throws IOException if there's no such a directory      */
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|getFilteredStatus
parameter_list|(
name|Predicate
argument_list|<
name|FileStatus
argument_list|>
name|function
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|FSUtils
operator|.
name|listStatusWithStatusFilter
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|status
lambda|->
name|function
operator|.
name|test
argument_list|(
name|status
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Perform a delete on a specified type.      * @param deletion a delete      * @param type possible values are 'files', 'subdirs', 'dirs'      * @return true if it deleted successfully, false otherwise      */
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
parameter_list|)
block|{
name|boolean
name|deleted
decl_stmt|;
name|String
name|errorMsg
init|=
literal|null
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
name|IOException
name|ioe
parameter_list|)
block|{
name|errorMsg
operator|=
name|ioe
operator|.
name|getMessage
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not delete {} under {}; {}"
argument_list|,
name|type
argument_list|,
name|dir
argument_list|,
name|errorMsg
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
comment|/**      * Get cleaner results of subdirs.      * @param tasks subdirs cleaner tasks      * @return true if all subdirs deleted successfully, false for patial/all failures      * @throws IOException something happen during computation      */
specifier|private
name|boolean
name|getCleanResult
parameter_list|(
name|List
argument_list|<
name|CleanerTask
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|cleaned
init|=
literal|true
decl_stmt|;
try|try
block|{
for|for
control|(
name|CleanerTask
name|task
range|:
name|tasks
control|)
block|{
name|cleaned
operator|&=
name|task
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
decl||
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|cleaned
return|;
block|}
block|}
block|}
end_class

end_unit

