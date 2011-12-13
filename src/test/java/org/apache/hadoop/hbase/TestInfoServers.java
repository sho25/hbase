begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|BufferedInputStream
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
name|net
operator|.
name|URL
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
name|client
operator|.
name|HTable
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
comment|/**  * Testing, info servers are disabled.  This test enables then and checks that  * they serve pages.  */
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
name|TestInfoServers
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestInfoServers
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// The info servers do not run in tests by default.
comment|// Set them to ephemeral ports so they will start
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.info.port"
argument_list|,
literal|0
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
name|afterClass
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
comment|/**    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testInfoServersRedirect
parameter_list|()
throws|throws
name|Exception
block|{
comment|// give the cluster time to start up
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|".META."
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|int
name|port
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getInfoServer
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|assertHasExpectedContent
argument_list|(
operator|new
name|URL
argument_list|(
literal|"http://localhost:"
operator|+
name|port
operator|+
literal|"/index.html"
argument_list|)
argument_list|,
literal|"master-status"
argument_list|)
expr_stmt|;
name|port
operator|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getInfoServer
argument_list|()
operator|.
name|getPort
argument_list|()
expr_stmt|;
name|assertHasExpectedContent
argument_list|(
operator|new
name|URL
argument_list|(
literal|"http://localhost:"
operator|+
name|port
operator|+
literal|"/index.html"
argument_list|)
argument_list|,
literal|"rs-status"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that the status pages in the minicluster load properly.    *    * This is somewhat a duplicate of TestRSStatusServlet and    * TestMasterStatusServlet, but those are true unit tests    * whereas this uses a cluster.    */
annotation|@
name|Test
specifier|public
name|void
name|testInfoServersStatusPages
parameter_list|()
throws|throws
name|Exception
block|{
comment|// give the cluster time to start up
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|".META."
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|int
name|port
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getInfoServer
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|assertHasExpectedContent
argument_list|(
operator|new
name|URL
argument_list|(
literal|"http://localhost:"
operator|+
name|port
operator|+
literal|"/master-status"
argument_list|)
argument_list|,
literal|"META"
argument_list|)
expr_stmt|;
name|port
operator|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getInfoServer
argument_list|()
operator|.
name|getPort
argument_list|()
expr_stmt|;
name|assertHasExpectedContent
argument_list|(
operator|new
name|URL
argument_list|(
literal|"http://localhost:"
operator|+
name|port
operator|+
literal|"/rs-status"
argument_list|)
argument_list|,
literal|"META"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertHasExpectedContent
parameter_list|(
specifier|final
name|URL
name|u
parameter_list|,
specifier|final
name|String
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Testing "
operator|+
name|u
operator|.
name|toString
argument_list|()
operator|+
literal|" has "
operator|+
name|expected
argument_list|)
expr_stmt|;
name|java
operator|.
name|net
operator|.
name|URLConnection
name|c
init|=
name|u
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|c
operator|.
name|connect
argument_list|()
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|BufferedInputStream
name|bis
init|=
operator|new
name|BufferedInputStream
argument_list|(
name|c
operator|.
name|getInputStream
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
for|for
control|(
name|int
name|read
init|=
operator|-
literal|1
init|;
operator|(
name|read
operator|=
name|bis
operator|.
name|read
argument_list|(
name|bytes
argument_list|)
operator|)
operator|!=
operator|-
literal|1
condition|;
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|read
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|bis
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|content
init|=
name|sb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected="
operator|+
name|expected
operator|+
literal|", content="
operator|+
name|content
argument_list|,
name|content
operator|.
name|contains
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

