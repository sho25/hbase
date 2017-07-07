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
name|List
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
name|Assert
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * A base class for tests that do something with the cluster while running  * {@link LoadTestTool} to write and verify some data.  */
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
name|IntegrationTestIngest
extends|extends
name|IntegrationTestBase
block|{
specifier|public
specifier|static
specifier|final
name|char
name|HIPHEN
init|=
literal|'-'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SERVER_COUNT
init|=
literal|1
decl_stmt|;
comment|// number of slaves for the smallest cluster
specifier|protected
specifier|static
specifier|final
name|long
name|DEFAULT_RUN_TIME
init|=
literal|20
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|JUNIT_RUN_TIME
init|=
literal|10
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
comment|/** A soft limit on how long we should run */
specifier|protected
specifier|static
specifier|final
name|String
name|RUN_TIME_KEY
init|=
literal|"hbase.%s.runtime"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|NUM_KEYS_PER_SERVER_KEY
init|=
literal|"num_keys_per_server"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|DEFAULT_NUM_KEYS_PER_SERVER
init|=
literal|2500
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|NUM_WRITE_THREADS_KEY
init|=
literal|"num_write_threads"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_WRITE_THREADS
init|=
literal|20
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|NUM_READ_THREADS_KEY
init|=
literal|"num_read_threads"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_READ_THREADS
init|=
literal|20
decl_stmt|;
comment|// Log is being used in IntegrationTestIngestWithEncryption, hence it is protected
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IntegrationTestIngest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|IntegrationTestingUtility
name|util
decl_stmt|;
specifier|protected
name|HBaseCluster
name|cluster
decl_stmt|;
specifier|protected
name|LoadTestTool
name|loadTool
decl_stmt|;
specifier|protected
name|String
index|[]
name|LOAD_TEST_TOOL_INIT_ARGS
init|=
block|{
name|LoadTestTool
operator|.
name|OPT_COLUMN_FAMILIES
block|,
name|LoadTestTool
operator|.
name|OPT_COMPRESSION
block|,
name|LoadTestTool
operator|.
name|OPT_DATA_BLOCK_ENCODING
block|,
name|LoadTestTool
operator|.
name|OPT_INMEMORY
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
specifier|public
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
name|getTestingUtil
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Initializing/checking cluster has "
operator|+
name|SERVER_COUNT
operator|+
literal|" servers"
argument_list|)
expr_stmt|;
name|util
operator|.
name|initializeCluster
argument_list|(
name|getMinServerCount
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Done initializing/checking cluster"
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
expr_stmt|;
name|deleteTableIfNecessary
argument_list|()
expr_stmt|;
name|loadTool
operator|=
operator|new
name|LoadTestTool
argument_list|()
expr_stmt|;
name|loadTool
operator|.
name|setConf
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// Initialize load test tool before we start breaking things;
comment|// LoadTestTool init, even when it is a no-op, is very fragile.
name|initTable
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|int
name|getMinServerCount
parameter_list|()
block|{
return|return
name|SERVER_COUNT
return|;
block|}
specifier|protected
name|void
name|initTable
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|ret
init|=
name|loadTool
operator|.
name|run
argument_list|(
name|getArgsForLoadTestToolInitTable
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Failed to initialize LoadTestTool"
argument_list|,
literal|0
argument_list|,
name|ret
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|runTestFromCommandLine
parameter_list|()
throws|throws
name|Exception
block|{
name|internalRunIngestTest
argument_list|(
name|DEFAULT_RUN_TIME
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
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
literal|2500
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
specifier|protected
name|void
name|internalRunIngestTest
parameter_list|(
name|long
name|runTime
parameter_list|)
throws|throws
name|Exception
block|{
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
name|long
name|numKeysPerServer
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|clazz
argument_list|,
name|NUM_KEYS_PER_SERVER_KEY
argument_list|)
argument_list|,
name|DEFAULT_NUM_KEYS_PER_SERVER
argument_list|)
decl_stmt|;
name|int
name|numWriteThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|clazz
argument_list|,
name|NUM_WRITE_THREADS_KEY
argument_list|)
argument_list|,
name|DEFAULT_NUM_WRITE_THREADS
argument_list|)
decl_stmt|;
name|int
name|numReadThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|clazz
argument_list|,
name|NUM_READ_THREADS_KEY
argument_list|)
argument_list|,
name|DEFAULT_NUM_READ_THREADS
argument_list|)
decl_stmt|;
name|runIngestTest
argument_list|(
name|runTime
argument_list|,
name|numKeysPerServer
argument_list|,
literal|10
argument_list|,
literal|1024
argument_list|,
name|numWriteThreads
argument_list|,
name|numReadThreads
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTablename
parameter_list|()
block|{
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
return|return
name|TableName
operator|.
name|valueOf
argument_list|(
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
name|LoadTestTool
operator|.
name|OPT_TABLE_NAME
argument_list|)
argument_list|,
name|clazz
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|getColumnFamilies
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|families
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
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
comment|// parse conf for getting the column famly names because LTT is not initialized yet.
name|String
name|familiesString
init|=
name|getConf
argument_list|()
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
name|LoadTestTool
operator|.
name|OPT_COLUMN_FAMILIES
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|familiesString
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|byte
index|[]
name|family
range|:
name|LoadTestTool
operator|.
name|DEFAULT_COLUMN_FAMILIES
control|)
block|{
name|families
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|String
name|family
range|:
name|familiesString
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|families
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|families
return|;
block|}
specifier|private
name|void
name|deleteTableIfNecessary
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|getTablename
argument_list|()
argument_list|)
condition|)
block|{
name|util
operator|.
name|deleteTable
argument_list|(
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|runIngestTest
parameter_list|(
name|long
name|defaultRunTime
parameter_list|,
name|long
name|keysPerServerPerIter
parameter_list|,
name|int
name|colsPerKey
parameter_list|,
name|int
name|recordSize
parameter_list|,
name|int
name|writeThreads
parameter_list|,
name|int
name|readThreads
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running ingest"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster size:"
operator|+
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|String
name|runtimeKey
init|=
name|String
operator|.
name|format
argument_list|(
name|RUN_TIME_KEY
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|runtime
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
name|runtimeKey
argument_list|,
name|defaultRunTime
argument_list|)
decl_stmt|;
name|long
name|startKey
init|=
literal|0
decl_stmt|;
name|long
name|numKeys
init|=
name|getNumKeys
argument_list|(
name|keysPerServerPerIter
argument_list|)
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|<
literal|0.9
operator|*
name|runtime
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Intended run time: "
operator|+
operator|(
name|runtime
operator|/
literal|60000
operator|)
operator|+
literal|" min, left:"
operator|+
operator|(
operator|(
name|runtime
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|)
operator|/
literal|60000
operator|)
operator|+
literal|" min"
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
operator|-
literal|1
decl_stmt|;
name|ret
operator|=
name|loadTool
operator|.
name|run
argument_list|(
name|getArgsForLoadTestTool
argument_list|(
literal|"-write"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%d:%d:%d"
argument_list|,
name|colsPerKey
argument_list|,
name|recordSize
argument_list|,
name|writeThreads
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ret
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Load failed with error code "
operator|+
name|ret
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|ret
operator|=
name|loadTool
operator|.
name|run
argument_list|(
name|getArgsForLoadTestTool
argument_list|(
literal|"-update"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"60:%d:1"
argument_list|,
name|writeThreads
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ret
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Update failed with error code "
operator|+
name|ret
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|ret
operator|=
name|loadTool
operator|.
name|run
argument_list|(
name|getArgsForLoadTestTool
argument_list|(
literal|"-read"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"100:%d"
argument_list|,
name|readThreads
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ret
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Verification failed with error code "
operator|+
name|ret
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
operator|+
literal|" Rerunning verification after 1 minute for debugging"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
operator|*
literal|60
argument_list|)
expr_stmt|;
name|ret
operator|=
name|loadTool
operator|.
name|run
argument_list|(
name|getArgsForLoadTestTool
argument_list|(
literal|"-read"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"100:%d"
argument_list|,
name|readThreads
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ret
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Rerun of Verification failed with error code "
operator|+
name|ret
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|fail
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|startKey
operator|+=
name|numKeys
expr_stmt|;
block|}
block|}
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
name|LOAD_TEST_TOOL_INIT_ARGS
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
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|11
argument_list|)
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
name|args
operator|.
name|add
argument_list|(
literal|"-families"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|getColumnFamiliesAsString
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|mode
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|modeSpecificArg
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-start_key"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|startKey
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-num_keys"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|numKeys
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-skip_init"
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
specifier|private
name|String
name|getColumnFamiliesAsString
parameter_list|()
block|{
return|return
name|StringUtils
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|getColumnFamilies
argument_list|()
argument_list|)
return|;
block|}
comment|/** Estimates a data size based on the cluster size */
specifier|protected
name|long
name|getNumKeys
parameter_list|(
name|long
name|keysPerServer
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|numRegionServers
init|=
name|cluster
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
decl_stmt|;
return|return
name|keysPerServer
operator|*
name|numRegionServers
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
name|IntegrationTestIngest
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

