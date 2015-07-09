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
name|HConstants
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
name|Threads
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
name|NoopProcedureStore
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
name|wal
operator|.
name|WALProcedureStore
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_class
specifier|public
class|class
name|ProcedureTestingUtility
block|{
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
name|ProcedureTestingUtility
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ProcedureTestingUtility
parameter_list|()
block|{   }
specifier|public
specifier|static
name|ProcedureStore
name|createStore
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|baseDir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createWalStore
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|baseDir
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|WALProcedureStore
name|createWalStore
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|logDir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|WALProcedureStore
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|logDir
argument_list|,
operator|new
name|WALProcedureStore
operator|.
name|LeaseRecovery
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|recoverFileLease
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no-op
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|restart
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|)
throws|throws
name|Exception
block|{
name|restart
argument_list|(
name|procExecutor
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|restart
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|Runnable
name|beforeStartAction
parameter_list|,
name|boolean
name|failOnCorrupted
parameter_list|)
throws|throws
name|Exception
block|{
name|ProcedureStore
name|procStore
init|=
name|procExecutor
operator|.
name|getStore
argument_list|()
decl_stmt|;
name|int
name|storeThreads
init|=
name|procExecutor
operator|.
name|getNumThreads
argument_list|()
decl_stmt|;
name|int
name|execThreads
init|=
name|procExecutor
operator|.
name|getNumThreads
argument_list|()
decl_stmt|;
comment|// stop
name|procExecutor
operator|.
name|stop
argument_list|()
expr_stmt|;
name|procExecutor
operator|.
name|join
argument_list|()
expr_stmt|;
name|procStore
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// nothing running...
if|if
condition|(
name|beforeStartAction
operator|!=
literal|null
condition|)
block|{
name|beforeStartAction
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
comment|// re-start
name|procStore
operator|.
name|start
argument_list|(
name|storeThreads
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|start
argument_list|(
name|execThreads
argument_list|,
name|failOnCorrupted
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|setKillBeforeStoreUpdate
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
name|procExecutor
operator|.
name|testing
operator|==
literal|null
condition|)
block|{
name|procExecutor
operator|.
name|testing
operator|=
operator|new
name|ProcedureExecutor
operator|.
name|Testing
argument_list|()
expr_stmt|;
block|}
name|procExecutor
operator|.
name|testing
operator|.
name|killBeforeStoreUpdate
operator|=
name|value
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Set Kill before store update to: "
operator|+
name|procExecutor
operator|.
name|testing
operator|.
name|killBeforeStoreUpdate
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|setToggleKillBeforeStoreUpdate
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
name|procExecutor
operator|.
name|testing
operator|==
literal|null
condition|)
block|{
name|procExecutor
operator|.
name|testing
operator|=
operator|new
name|ProcedureExecutor
operator|.
name|Testing
argument_list|()
expr_stmt|;
block|}
name|procExecutor
operator|.
name|testing
operator|.
name|toggleKillBeforeStoreUpdate
operator|=
name|value
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|toggleKillBeforeStoreUpdate
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|)
block|{
if|if
condition|(
name|procExecutor
operator|.
name|testing
operator|==
literal|null
condition|)
block|{
name|procExecutor
operator|.
name|testing
operator|=
operator|new
name|ProcedureExecutor
operator|.
name|Testing
argument_list|()
expr_stmt|;
block|}
name|procExecutor
operator|.
name|testing
operator|.
name|killBeforeStoreUpdate
operator|=
operator|!
name|procExecutor
operator|.
name|testing
operator|.
name|killBeforeStoreUpdate
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Set Kill before store update to: "
operator|+
name|procExecutor
operator|.
name|testing
operator|.
name|killBeforeStoreUpdate
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|setKillAndToggleBeforeStoreUpdate
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
name|ProcedureTestingUtility
operator|.
name|setKillBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setToggleKillBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|long
name|submitAndWait
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|TEnv
name|env
parameter_list|,
name|Procedure
argument_list|<
name|TEnv
argument_list|>
name|proc
parameter_list|)
throws|throws
name|IOException
block|{
name|NoopProcedureStore
name|procStore
init|=
operator|new
name|NoopProcedureStore
argument_list|()
decl_stmt|;
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
init|=
operator|new
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
argument_list|(
name|conf
argument_list|,
name|env
argument_list|,
name|procStore
argument_list|)
decl_stmt|;
name|procStore
operator|.
name|start
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|start
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|,
name|HConstants
operator|.
name|NO_NONCE
argument_list|,
name|HConstants
operator|.
name|NO_NONCE
argument_list|)
return|;
block|}
finally|finally
block|{
name|procStore
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|long
name|submitAndWait
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|Procedure
name|proc
parameter_list|)
block|{
return|return
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|,
name|HConstants
operator|.
name|NO_NONCE
argument_list|,
name|HConstants
operator|.
name|NO_NONCE
argument_list|)
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|long
name|submitAndWait
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|Procedure
name|proc
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
block|{
name|long
name|procId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
expr_stmt|;
return|return
name|procId
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|waitProcedure
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|long
name|procId
parameter_list|)
block|{
while|while
condition|(
operator|!
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
operator|&&
name|procExecutor
operator|.
name|isRunning
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|250
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|waitNoProcedureRunning
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|)
block|{
name|int
name|stableRuns
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|stableRuns
operator|<
literal|10
condition|)
block|{
if|if
condition|(
name|procExecutor
operator|.
name|getActiveExecutorCount
argument_list|()
operator|>
literal|0
operator|||
name|procExecutor
operator|.
name|getRunnableSet
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|stableRuns
operator|=
literal|0
expr_stmt|;
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stableRuns
operator|++
expr_stmt|;
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|25
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|assertProcNotYetCompleted
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|long
name|procId
parameter_list|)
block|{
name|assertFalse
argument_list|(
literal|"expected a running proc"
argument_list|,
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|procExecutor
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TEnv
parameter_list|>
name|void
name|assertProcNotFailed
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnv
argument_list|>
name|procExecutor
parameter_list|,
name|long
name|procId
parameter_list|)
block|{
name|ProcedureResult
name|result
init|=
name|procExecutor
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected procedure result"
argument_list|,
name|result
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertProcNotFailed
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertProcNotFailed
parameter_list|(
specifier|final
name|ProcedureResult
name|result
parameter_list|)
block|{
name|Exception
name|exception
init|=
name|result
operator|.
name|getException
argument_list|()
decl_stmt|;
name|String
name|msg
init|=
name|exception
operator|!=
literal|null
condition|?
name|exception
operator|.
name|toString
argument_list|()
else|:
literal|"no exception found"
decl_stmt|;
name|assertFalse
argument_list|(
name|msg
argument_list|,
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertIsAbortException
parameter_list|(
specifier|final
name|ProcedureResult
name|result
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|result
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|cause
init|=
name|result
operator|.
name|getException
argument_list|()
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected abort exception, got "
operator|+
name|cause
argument_list|,
name|cause
operator|instanceof
name|ProcedureAbortedException
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

