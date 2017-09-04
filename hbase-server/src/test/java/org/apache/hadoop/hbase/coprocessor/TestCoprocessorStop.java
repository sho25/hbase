begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|hbase
operator|.
name|*
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
name|hbase
operator|.
name|testclassification
operator|.
name|CoprocessorTests
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
name|junit
operator|.
name|AfterClass
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

begin_comment
comment|/**  * Tests for master and regionserver coprocessor stop method  *  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCoprocessorStop
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
name|TestCoprocessorStop
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MASTER_FILE
init|=
literal|"master"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REGIONSERVER_FILE
init|=
literal|"regionserver"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|public
specifier|static
class|class
name|FooCoprocessor
implements|implements
name|MasterCoprocessor
implements|,
name|RegionServerCoprocessor
block|{
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|where
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|env
operator|instanceof
name|MasterCoprocessorEnvironment
condition|)
block|{
comment|// if running on HMaster
name|where
operator|=
literal|"master"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|env
operator|instanceof
name|RegionServerCoprocessorEnvironment
condition|)
block|{
name|where
operator|=
literal|"regionserver"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"on RegionCoprocessorEnvironment!!"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"start coprocessor on "
operator|+
name|where
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fileName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|env
operator|instanceof
name|MasterCoprocessorEnvironment
condition|)
block|{
comment|// if running on HMaster
name|fileName
operator|=
name|MASTER_FILE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|env
operator|instanceof
name|RegionServerCoprocessorEnvironment
condition|)
block|{
name|fileName
operator|=
name|REGIONSERVER_FILE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"on RegionCoprocessorEnvironment!!"
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|resultFile
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|result
init|=
name|fs
operator|.
name|createNewFile
argument_list|(
name|resultFile
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create file "
operator|+
name|resultFile
operator|+
literal|" return rc "
operator|+
name|result
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|FooCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|,
name|FooCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStopped
parameter_list|()
throws|throws
name|Exception
block|{
comment|//shutdown hbase only. then check flag file.
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"shutdown hbase cluster..."
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"wait for the hbase cluster shutdown..."
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitUntilShutDown
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|resultFile
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|,
name|MASTER_FILE
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Master flag file should have been created"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|resultFile
argument_list|)
argument_list|)
expr_stmt|;
name|resultFile
operator|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|,
name|REGIONSERVER_FILE
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"RegionServer flag file should have been created"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|resultFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

