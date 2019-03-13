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
name|List
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
name|client
operator|.
name|Admin
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
name|ColumnFamilyDescriptor
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
name|ColumnFamilyDescriptorBuilder
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
name|Connection
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
name|ConnectionFactory
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
name|IntegrationTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|HFileTestUtil
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
name|LoadTestDataGeneratorWithMOB
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
name|LoadTestTool
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
name|util
operator|.
name|ToolRunner
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
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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

begin_comment
comment|/**  * Integration Test for MOB ingest.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestIngestWithMOB
extends|extends
name|IntegrationTestIngest
block|{
specifier|private
specifier|static
specifier|final
name|char
name|COLON
init|=
literal|':'
decl_stmt|;
specifier|private
name|byte
index|[]
name|mobColumnFamily
init|=
name|HFileTestUtil
operator|.
name|DEFAULT_COLUMN_FAMILY
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|THRESHOLD
init|=
literal|"threshold"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIN_MOB_DATA_SIZE
init|=
literal|"minMobDataSize"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_MOB_DATA_SIZE
init|=
literal|"maxMobDataSize"
decl_stmt|;
specifier|private
name|int
name|threshold
init|=
literal|1024
decl_stmt|;
comment|// 1KB
specifier|private
name|int
name|minMobDataSize
init|=
literal|512
decl_stmt|;
comment|// 512B
specifier|private
name|int
name|maxMobDataSize
init|=
name|threshold
operator|*
literal|5
decl_stmt|;
comment|// 5KB
specifier|private
specifier|static
specifier|final
name|long
name|JUNIT_RUN_TIME
init|=
literal|2
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
comment|// 2 minutes
comment|//similar to LOAD_TEST_TOOL_INIT_ARGS except OPT_IN_MEMORY is removed
specifier|protected
name|String
index|[]
name|LOAD_TEST_TOOL_MOB_INIT_ARGS
init|=
block|{
name|LoadTestTool
operator|.
name|OPT_COMPRESSION
block|,
name|HFileTestUtil
operator|.
name|OPT_DATA_BLOCK_ENCODING
block|,
name|LoadTestTool
operator|.
name|OPT_ENCRYPTION
block|,
name|LoadTestTool
operator|.
name|OPT_NUM_REGIONS_PER_SERVER
block|,
name|LoadTestTool
operator|.
name|OPT_REGION_REPLICATION
block|,   }
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|String
index|[]
name|getArgsForLoadTestToolInitTable
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-tn"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|getTablename
argument_list|()
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
comment|// pass all remaining args from conf with keys<test class name>.<load test tool arg>
name|String
name|clazz
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|LOAD_TEST_TOOL_MOB_INIT_ARGS
control|)
block|{
name|String
name|val
init|=
name|conf
operator|.
name|get
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|clazz
argument_list|,
name|arg
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|arg
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
block|}
name|args
operator|.
name|add
argument_list|(
literal|"-init_only"
argument_list|)
expr_stmt|;
return|return
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|args
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|super
operator|.
name|addOptions
argument_list|()
expr_stmt|;
name|super
operator|.
name|addOptWithArg
argument_list|(
name|THRESHOLD
argument_list|,
literal|"The threshold to classify cells to mob data"
argument_list|)
expr_stmt|;
name|super
operator|.
name|addOptWithArg
argument_list|(
name|MIN_MOB_DATA_SIZE
argument_list|,
literal|"Minimum value size for mob data"
argument_list|)
expr_stmt|;
name|super
operator|.
name|addOptWithArg
argument_list|(
name|MAX_MOB_DATA_SIZE
argument_list|,
literal|"Maximum value size for mob data"
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
name|super
operator|.
name|processOptions
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|THRESHOLD
argument_list|)
condition|)
block|{
name|threshold
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|THRESHOLD
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|MIN_MOB_DATA_SIZE
argument_list|)
condition|)
block|{
name|minMobDataSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|MIN_MOB_DATA_SIZE
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|MAX_MOB_DATA_SIZE
argument_list|)
condition|)
block|{
name|maxMobDataSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|MAX_MOB_DATA_SIZE
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minMobDataSize
operator|>
name|maxMobDataSize
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The minMobDataSize should not be larger than minMobDataSize"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIngest
parameter_list|()
throws|throws
name|Exception
block|{
name|runIngestTest
argument_list|(
name|JUNIT_RUN_TIME
argument_list|,
literal|100
argument_list|,
literal|10
argument_list|,
literal|1024
argument_list|,
literal|10
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initTable
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|initTable
argument_list|()
expr_stmt|;
name|TableName
name|tableName
init|=
name|getTablename
argument_list|()
decl_stmt|;
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|()
init|;
name|Admin
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|admin
operator|.
name|getDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Disabling table "
operator|+
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|ColumnFamilyDescriptor
name|mobColumn
init|=
name|tableDesc
operator|.
name|getColumnFamily
argument_list|(
name|mobColumnFamily
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|cfd
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|mobColumn
argument_list|)
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
operator|.
name|setMobThreshold
argument_list|(
operator|(
name|long
operator|)
name|threshold
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|tableName
argument_list|,
name|cfd
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Enabling table "
operator|+
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|String
index|[]
name|getArgsForLoadTestTool
parameter_list|(
name|String
name|mode
parameter_list|,
name|String
name|modeSpecificArg
parameter_list|,
name|long
name|startKey
parameter_list|,
name|long
name|numKeys
parameter_list|)
block|{
name|String
index|[]
name|args
init|=
name|super
operator|.
name|getArgsForLoadTestTool
argument_list|(
name|mode
argument_list|,
name|modeSpecificArg
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tmp
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
decl_stmt|;
comment|// LoadTestDataGeneratorMOB:mobColumnFamily:minMobDataSize:maxMobDataSize
name|tmp
operator|.
name|add
argument_list|(
name|HIPHEN
operator|+
name|LoadTestTool
operator|.
name|OPT_GENERATOR
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|LoadTestDataGeneratorWithMOB
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|mobColumnFamily
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|minMobDataSize
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|maxMobDataSize
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tmp
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|tmp
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
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
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestIngestWithMOB
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

