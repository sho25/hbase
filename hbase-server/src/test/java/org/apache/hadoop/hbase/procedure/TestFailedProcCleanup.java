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
name|procedure
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
operator|.
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|hbase
operator|.
name|HBaseClassTestRule
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
name|HBaseTestingUtility
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
name|TableName
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
name|client
operator|.
name|RegionInfo
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
name|client
operator|.
name|TableDescriptor
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
name|coprocessor
operator|.
name|MasterCoprocessor
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|MasterObserver
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
name|coprocessor
operator|.
name|ObserverContext
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
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureEnv
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
name|Procedure
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
name|security
operator|.
name|AccessDeniedException
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
name|testclassification
operator|.
name|MediumTests
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
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * Check if CompletedProcedureCleaner cleans up failed nonce procedures.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestFailedProcCleanup
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestFailedProcCleanup
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestFailedProcCleanup
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|evictionDelay
init|=
literal|10
operator|*
literal|1000
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.procedure.cleaner.evict.ttl"
argument_list|,
name|evictionDelay
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.procedure.cleaner.evict.batch.size"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailCreateTable
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|set
argument_list|(
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|CreateFailObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
try|try
block|{
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AccessDeniedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring exception: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|evictionDelay
operator|*
literal|3
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
argument_list|>
name|procedureInfos
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getProcedures
argument_list|()
decl_stmt|;
for|for
control|(
name|Procedure
name|procedureInfo
range|:
name|procedureInfos
control|)
block|{
if|if
condition|(
name|procedureInfo
operator|.
name|getProcName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"CreateTableProcedure"
argument_list|)
operator|&&
name|procedureInfo
operator|.
name|getState
argument_list|()
operator|==
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|ROLLEDBACK
condition|)
block|{
name|fail
argument_list|(
literal|"Found procedure "
operator|+
name|procedureInfo
operator|+
literal|" that hasn't been cleaned up"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailCreateTableAction
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|set
argument_list|(
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|CreateFailObserverHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
try|try
block|{
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Table shouldn't be created"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AccessDeniedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring exception: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|evictionDelay
operator|*
literal|3
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
argument_list|>
name|procedureInfos
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getProcedures
argument_list|()
decl_stmt|;
for|for
control|(
name|Procedure
name|procedureInfo
range|:
name|procedureInfos
control|)
block|{
if|if
condition|(
name|procedureInfo
operator|.
name|getProcName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"CreateTableProcedure"
argument_list|)
operator|&&
name|procedureInfo
operator|.
name|getState
argument_list|()
operator|==
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|ROLLEDBACK
condition|)
block|{
name|fail
argument_list|(
literal|"Found procedure "
operator|+
name|procedureInfo
operator|+
literal|" that hasn't been cleaned up"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|CreateFailObserver
implements|implements
name|MasterCoprocessor
implements|,
name|MasterObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|TableDescriptor
name|desc
parameter_list|,
name|RegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|desc
operator|.
name|getTableName
argument_list|()
operator|.
name|equals
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AccessDeniedException
argument_list|(
literal|"Don't allow creation of table"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|MasterObserver
argument_list|>
name|getMasterObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|CreateFailObserverHandler
implements|implements
name|MasterCoprocessor
implements|,
name|MasterObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preCreateTableAction
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|TableDescriptor
name|desc
parameter_list|,
specifier|final
name|RegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|desc
operator|.
name|getTableName
argument_list|()
operator|.
name|equals
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AccessDeniedException
argument_list|(
literal|"Don't allow creation of table"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|MasterObserver
argument_list|>
name|getMasterObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

