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
name|procedure2
operator|.
name|store
operator|.
name|wal
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
name|Collections
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
name|Random
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
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
name|cli
operator|.
name|Option
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
name|HBaseCommonTestingUtility
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
name|procedure2
operator|.
name|ProcedureTestingUtility
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
name|procedure2
operator|.
name|ProcedureTestingUtility
operator|.
name|TestProcedure
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
name|procedure2
operator|.
name|store
operator|.
name|ProcedureStore
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
name|procedure2
operator|.
name|store
operator|.
name|ProcedureStore
operator|.
name|ProcedureIterator
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
name|procedure2
operator|.
name|util
operator|.
name|StringUtils
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
name|AbstractHBaseTool
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|System
operator|.
name|currentTimeMillis
import|;
end_import

begin_class
specifier|public
class|class
name|ProcedureWALLoaderPerformanceEvaluation
extends|extends
name|AbstractHBaseTool
block|{
specifier|protected
specifier|static
specifier|final
name|HBaseCommonTestingUtility
name|UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
comment|// Command line options and defaults.
specifier|public
specifier|static
name|int
name|DEFAULT_NUM_PROCS
init|=
literal|1000000
decl_stmt|;
comment|// 1M
specifier|public
specifier|static
name|Option
name|NUM_PROCS_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"procs"
argument_list|,
literal|true
argument_list|,
literal|"Total number of procedures. Default: "
operator|+
name|DEFAULT_NUM_PROCS
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|DEFAULT_NUM_WALS
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
name|Option
name|NUM_WALS_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"wals"
argument_list|,
literal|true
argument_list|,
literal|"Number of WALs to write. If -ve or 0, uses "
operator|+
name|WALProcedureStore
operator|.
name|ROLL_THRESHOLD_CONF_KEY
operator|+
literal|" conf to roll the logs. Default: "
operator|+
name|DEFAULT_NUM_WALS
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|DEFAULT_STATE_SIZE
init|=
literal|1024
decl_stmt|;
comment|// 1KB
specifier|public
specifier|static
name|Option
name|STATE_SIZE_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"state_size"
argument_list|,
literal|true
argument_list|,
literal|"Size of serialized state in bytes to write on update. Default: "
operator|+
name|DEFAULT_STATE_SIZE
operator|+
literal|" bytes"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|DEFAULT_UPDATES_PER_PROC
init|=
literal|5
decl_stmt|;
specifier|public
specifier|static
name|Option
name|UPDATES_PER_PROC_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"updates_per_proc"
argument_list|,
literal|true
argument_list|,
literal|"Number of update states to write for each proc. Default: "
operator|+
name|DEFAULT_UPDATES_PER_PROC
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|double
name|DEFAULT_DELETE_PROCS_FRACTION
init|=
literal|0.50
decl_stmt|;
specifier|public
specifier|static
name|Option
name|DELETE_PROCS_FRACTION_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"delete_procs_fraction"
argument_list|,
literal|true
argument_list|,
literal|"Fraction of procs for which to write delete state. Distribution of procs chosen for "
operator|+
literal|"delete is uniform across all procs. Default: "
operator|+
name|DEFAULT_DELETE_PROCS_FRACTION
argument_list|)
decl_stmt|;
specifier|public
name|int
name|numProcs
decl_stmt|;
specifier|public
name|int
name|updatesPerProc
decl_stmt|;
specifier|public
name|double
name|deleteProcsFraction
decl_stmt|;
specifier|public
name|int
name|numWals
decl_stmt|;
specifier|private
name|WALProcedureStore
name|store
decl_stmt|;
specifier|static
name|byte
index|[]
name|serializedState
decl_stmt|;
specifier|private
specifier|static
class|class
name|LoadCounter
implements|implements
name|ProcedureStore
operator|.
name|ProcedureLoader
block|{
specifier|public
name|LoadCounter
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|setMaxProcId
parameter_list|(
name|long
name|maxProcId
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|load
parameter_list|(
name|ProcedureIterator
name|procIter
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|procIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|procIter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleCorrupted
parameter_list|(
name|ProcedureIterator
name|procIter
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|procIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|procIter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOption
argument_list|(
name|NUM_PROCS_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|UPDATES_PER_PROC_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|DELETE_PROCS_FRACTION_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|NUM_WALS_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|STATE_SIZE_OPTION
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|numProcs
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|NUM_PROCS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_NUM_PROCS
argument_list|)
expr_stmt|;
name|numWals
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|NUM_WALS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_NUM_WALS
argument_list|)
expr_stmt|;
name|int
name|stateSize
init|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|STATE_SIZE_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_STATE_SIZE
argument_list|)
decl_stmt|;
name|serializedState
operator|=
operator|new
name|byte
index|[
name|stateSize
index|]
expr_stmt|;
name|updatesPerProc
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|UPDATES_PER_PROC_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_UPDATES_PER_PROC
argument_list|)
expr_stmt|;
name|deleteProcsFraction
operator|=
name|getOptionAsDouble
argument_list|(
name|cmd
argument_list|,
name|DELETE_PROCS_FRACTION_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_DELETE_PROCS_FRACTION
argument_list|)
expr_stmt|;
name|setupConf
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|setupConf
parameter_list|()
block|{
if|if
condition|(
name|numWals
operator|>
literal|0
condition|)
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|WALProcedureStore
operator|.
name|ROLL_THRESHOLD_CONF_KEY
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setUpProcedureStore
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|testDir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|logDir
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"proc-logs"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n\nLogs directory : "
operator|+
name|logDir
operator|.
name|toString
argument_list|()
operator|+
literal|"\n\n"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|logDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|store
operator|=
name|ProcedureTestingUtility
operator|.
name|createWalStore
argument_list|(
name|conf
argument_list|,
name|logDir
argument_list|)
expr_stmt|;
name|store
operator|.
name|start
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|store
operator|.
name|recoverLease
argument_list|()
expr_stmt|;
name|store
operator|.
name|load
argument_list|(
operator|new
name|LoadCounter
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return a list of shuffled integers which represent state of proc id. First occurrence of a    * number denotes insert state, consecutive occurrences denote update states, and -ve value    * denotes delete state.    */
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|shuffleProcWriteSequence
parameter_list|()
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|procStatesSequence
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|toBeDeletedProcs
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Add n + 1 entries of the proc id for insert + updates. If proc is chosen for delete, add
comment|// extra entry which is marked -ve in the loop after shuffle.
for|for
control|(
name|int
name|procId
init|=
literal|1
init|;
name|procId
operator|<=
name|numProcs
condition|;
operator|++
name|procId
control|)
block|{
name|procStatesSequence
operator|.
name|addAll
argument_list|(
name|Collections
operator|.
name|nCopies
argument_list|(
name|updatesPerProc
operator|+
literal|1
argument_list|,
name|procId
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|rand
operator|.
name|nextFloat
argument_list|()
operator|<
name|deleteProcsFraction
condition|)
block|{
name|procStatesSequence
operator|.
name|add
argument_list|(
name|procId
argument_list|)
expr_stmt|;
name|toBeDeletedProcs
operator|.
name|add
argument_list|(
name|procId
argument_list|)
expr_stmt|;
block|}
block|}
name|Collections
operator|.
name|shuffle
argument_list|(
name|procStatesSequence
argument_list|)
expr_stmt|;
comment|// Mark last occurrences of proc ids in toBeDeletedProcs with -ve to denote it's a delete state.
for|for
control|(
name|int
name|i
init|=
name|procStatesSequence
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|int
name|procId
init|=
name|procStatesSequence
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|toBeDeletedProcs
operator|.
name|contains
argument_list|(
name|procId
argument_list|)
condition|)
block|{
name|procStatesSequence
operator|.
name|set
argument_list|(
name|i
argument_list|,
operator|-
literal|1
operator|*
name|procId
argument_list|)
expr_stmt|;
name|toBeDeletedProcs
operator|.
name|remove
argument_list|(
name|procId
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|procStatesSequence
return|;
block|}
specifier|private
name|void
name|writeWals
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|procStates
init|=
name|shuffleProcWriteSequence
argument_list|()
decl_stmt|;
name|TestProcedure
index|[]
name|procs
init|=
operator|new
name|TestProcedure
index|[
name|numProcs
operator|+
literal|1
index|]
decl_stmt|;
comment|// 0 is not used.
name|int
name|numProcsPerWal
init|=
name|numWals
operator|>
literal|0
condition|?
name|procStates
operator|.
name|size
argument_list|()
operator|/
name|numWals
else|:
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|startTime
init|=
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|lastTime
init|=
name|startTime
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|procStates
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|int
name|procId
init|=
name|procStates
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|procId
operator|<
literal|0
condition|)
block|{
name|store
operator|.
name|delete
argument_list|(
name|procs
index|[
operator|-
name|procId
index|]
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|procs
index|[
operator|-
name|procId
index|]
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|procs
index|[
name|procId
index|]
operator|==
literal|null
condition|)
block|{
name|procs
index|[
name|procId
index|]
operator|=
operator|new
name|TestProcedure
argument_list|(
name|procId
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|procs
index|[
name|procId
index|]
operator|.
name|setData
argument_list|(
name|serializedState
argument_list|)
expr_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|procs
index|[
name|procId
index|]
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|store
operator|.
name|update
argument_list|(
name|procs
index|[
name|procId
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|>
literal|0
operator|&&
name|i
operator|%
name|numProcsPerWal
operator|==
literal|0
condition|)
block|{
name|long
name|currentTime
init|=
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Forcing wall roll. Time taken on last WAL: "
operator|+
operator|(
name|currentTime
operator|-
name|lastTime
operator|)
operator|/
literal|1000.0f
operator|+
literal|" sec"
argument_list|)
expr_stmt|;
name|store
operator|.
name|rollWriterForTesting
argument_list|()
expr_stmt|;
name|lastTime
operator|=
name|currentTime
expr_stmt|;
block|}
block|}
name|long
name|timeTaken
init|=
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n\nDone writing WALs.\nNum procs : "
operator|+
name|numProcs
operator|+
literal|"\nTotal time taken : "
operator|+
name|StringUtils
operator|.
name|humanTimeDiff
argument_list|(
name|timeTaken
argument_list|)
operator|+
literal|"\n\n"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|storeRestart
parameter_list|(
name|ProcedureStore
operator|.
name|ProcedureLoader
name|loader
parameter_list|)
throws|throws
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Restarting procedure store to read back the WALs"
argument_list|)
expr_stmt|;
name|store
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|store
operator|.
name|start
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|store
operator|.
name|recoverLease
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|store
operator|.
name|load
argument_list|(
name|loader
argument_list|)
expr_stmt|;
name|long
name|timeTaken
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"******************************************"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Load time : "
operator|+
operator|(
name|timeTaken
operator|/
literal|1000.0f
operator|)
operator|+
literal|"sec"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"******************************************"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Raw format for scripts"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"RESULT [%s=%s, %s=%s, %s=%s, %s=%s, %s=%s, "
operator|+
literal|"total_time_ms=%s]"
argument_list|,
name|NUM_PROCS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|numProcs
argument_list|,
name|STATE_SIZE_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|serializedState
operator|.
name|length
argument_list|,
name|UPDATES_PER_PROC_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|updatesPerProc
argument_list|,
name|DELETE_PROCS_FRACTION_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|deleteProcsFraction
argument_list|,
name|NUM_WALS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|numWals
argument_list|,
name|timeTaken
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|tearDownProcedureStore
parameter_list|()
block|{
name|store
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|store
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|store
operator|.
name|getWALDir
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error: Couldn't delete log dir. You can delete it manually to free up "
operator|+
literal|"disk space. Location: "
operator|+
name|store
operator|.
name|getWALDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
block|{
try|try
block|{
name|setUpProcedureStore
argument_list|()
expr_stmt|;
name|writeWals
argument_list|()
expr_stmt|;
name|storeRestart
argument_list|(
operator|new
name|LoadCounter
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|EXIT_SUCCESS
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
finally|finally
block|{
name|tearDownProcedureStore
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|ProcedureWALLoaderPerformanceEvaluation
name|tool
init|=
operator|new
name|ProcedureWALLoaderPerformanceEvaluation
argument_list|()
decl_stmt|;
name|tool
operator|.
name|setConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

