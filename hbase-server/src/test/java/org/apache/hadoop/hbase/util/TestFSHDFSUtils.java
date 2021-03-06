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
name|util
package|;
end_package

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
name|HBaseConfiguration
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
name|testclassification
operator|.
name|MiscTests
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
name|hdfs
operator|.
name|DistributedFileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|mockito
operator|.
name|Mockito
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
comment|/**  * Test our recoverLease loop against mocked up filesystem.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestFSHDFSUtils
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
name|TestFSHDFSUtils
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
name|TestFSHDFSUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
static|static
block|{
name|Configuration
name|conf
init|=
name|HTU
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.lease.recovery.first.pause"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.lease.recovery.pause"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
specifier|private
name|FSHDFSUtils
name|fsHDFSUtils
init|=
operator|new
name|FSHDFSUtils
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Path
name|FILE
init|=
operator|new
name|Path
argument_list|(
name|HTU
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"file.txt"
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|this
operator|.
name|startTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test recover lease eventually succeeding.    */
annotation|@
name|Test
specifier|public
name|void
name|testRecoverLease
parameter_list|()
throws|throws
name|IOException
block|{
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.lease.recovery.dfs.timeout"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|CancelableProgressable
name|reporter
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|CancelableProgressable
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|reporter
operator|.
name|progress
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|DistributedFileSystem
name|dfs
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|DistributedFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Fail four times and pass on the fifth.
name|Mockito
operator|.
name|when
argument_list|(
name|dfs
operator|.
name|recoverLease
argument_list|(
name|FILE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|this
operator|.
name|fsHDFSUtils
operator|.
name|recoverDFSFileLease
argument_list|(
name|dfs
argument_list|,
name|FILE
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|reporter
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|dfs
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|5
argument_list|)
argument_list|)
operator|.
name|recoverLease
argument_list|(
name|FILE
argument_list|)
expr_stmt|;
comment|// Make sure we waited at least hbase.lease.recovery.dfs.timeout * 3 (the first two
comment|// invocations will happen pretty fast... the we fall into the longer wait loop).
name|assertTrue
argument_list|(
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|this
operator|.
name|startTime
operator|)
operator|>
operator|(
literal|3
operator|*
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.lease.recovery.dfs.timeout"
argument_list|,
literal|61000
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that isFileClosed makes us recover lease faster.    */
annotation|@
name|Test
specifier|public
name|void
name|testIsFileClosed
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Make this time long so it is plain we broke out because of the isFileClosed invocation.
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.lease.recovery.dfs.timeout"
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|CancelableProgressable
name|reporter
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|CancelableProgressable
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|reporter
operator|.
name|progress
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|IsFileClosedDistributedFileSystem
name|dfs
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|IsFileClosedDistributedFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Now make it so we fail the first two times -- the two fast invocations, then we fall into
comment|// the long loop during which we will call isFileClosed.... the next invocation should
comment|// therefore return true if we are to break the loop.
name|Mockito
operator|.
name|when
argument_list|(
name|dfs
operator|.
name|recoverLease
argument_list|(
name|FILE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|dfs
operator|.
name|isFileClosed
argument_list|(
name|FILE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|this
operator|.
name|fsHDFSUtils
operator|.
name|recoverDFSFileLease
argument_list|(
name|dfs
argument_list|,
name|FILE
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|reporter
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|dfs
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|recoverLease
argument_list|(
name|FILE
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|dfs
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|isFileClosed
argument_list|(
name|FILE
argument_list|)
expr_stmt|;
block|}
name|void
name|testIsSameHdfs
parameter_list|(
name|int
name|nnport
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Class
name|dfsUtilClazz
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hdfs.DFSUtil"
argument_list|)
decl_stmt|;
name|dfsUtilClazz
operator|.
name|getMethod
argument_list|(
literal|"getNNServiceRpcAddresses"
argument_list|,
name|Configuration
operator|.
name|class
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
name|info
argument_list|(
literal|"Skip testIsSameHdfs test case because of the no-HA hadoop version."
argument_list|)
expr_stmt|;
return|return;
block|}
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|Path
name|srcPath
init|=
operator|new
name|Path
argument_list|(
literal|"hdfs://localhost:"
operator|+
name|nnport
operator|+
literal|"/"
argument_list|)
decl_stmt|;
name|Path
name|desPath
init|=
operator|new
name|Path
argument_list|(
literal|"hdfs://127.0.0.1/"
argument_list|)
decl_stmt|;
name|FileSystem
name|srcFs
init|=
name|srcPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|desFs
init|=
name|desPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|FSHDFSUtils
operator|.
name|isSameHdfs
argument_list|(
name|conf
argument_list|,
name|srcFs
argument_list|,
name|desFs
argument_list|)
argument_list|)
expr_stmt|;
name|desPath
operator|=
operator|new
name|Path
argument_list|(
literal|"hdfs://127.0.0.1:8070/"
argument_list|)
expr_stmt|;
name|desFs
operator|=
name|desPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|FSHDFSUtils
operator|.
name|isSameHdfs
argument_list|(
name|conf
argument_list|,
name|srcFs
argument_list|,
name|desFs
argument_list|)
argument_list|)
expr_stmt|;
name|desPath
operator|=
operator|new
name|Path
argument_list|(
literal|"hdfs://127.0.1.1:"
operator|+
name|nnport
operator|+
literal|"/"
argument_list|)
expr_stmt|;
name|desFs
operator|=
name|desPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|FSHDFSUtils
operator|.
name|isSameHdfs
argument_list|(
name|conf
argument_list|,
name|srcFs
argument_list|,
name|desFs
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
literal|"hdfs://haosong-hadoop"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.nameservices"
argument_list|,
literal|"haosong-hadoop"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.ha.namenodes.haosong-hadoop"
argument_list|,
literal|"nn1,nn2"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.client.failover.proxy.provider.haosong-hadoop"
argument_list|,
literal|"org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.namenode.rpc-address.haosong-hadoop.nn1"
argument_list|,
literal|"127.0.0.1:"
operator|+
name|nnport
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.namenode.rpc-address.haosong-hadoop.nn2"
argument_list|,
literal|"127.10.2.1:8000"
argument_list|)
expr_stmt|;
name|desPath
operator|=
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|desFs
operator|=
name|desPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|FSHDFSUtils
operator|.
name|isSameHdfs
argument_list|(
name|conf
argument_list|,
name|srcFs
argument_list|,
name|desFs
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.namenode.rpc-address.haosong-hadoop.nn1"
argument_list|,
literal|"127.10.2.1:"
operator|+
name|nnport
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.namenode.rpc-address.haosong-hadoop.nn2"
argument_list|,
literal|"127.0.0.1:8000"
argument_list|)
expr_stmt|;
name|desPath
operator|=
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|desFs
operator|=
name|desPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|FSHDFSUtils
operator|.
name|isSameHdfs
argument_list|(
name|conf
argument_list|,
name|srcFs
argument_list|,
name|desFs
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIsSameHdfs
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|hadoopVersion
init|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getVersion
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"hadoop version is: "
operator|+
name|hadoopVersion
argument_list|)
expr_stmt|;
name|boolean
name|isHadoop3_0_0
init|=
name|hadoopVersion
operator|.
name|startsWith
argument_list|(
literal|"3.0.0"
argument_list|)
decl_stmt|;
if|if
condition|(
name|isHadoop3_0_0
condition|)
block|{
comment|// Hadoop 3.0.0 alpha1+ ~ 3.0.0 GA changed default nn port to 9820.
comment|// See HDFS-9427
name|testIsSameHdfs
argument_list|(
literal|9820
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// pre hadoop 3.0.0 defaults to port 8020
comment|// Hadoop 3.0.1 changed it back to port 8020. See HDFS-12990
name|testIsSameHdfs
argument_list|(
literal|8020
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Version of DFS that has HDFS-4525 in it.    */
specifier|static
class|class
name|IsFileClosedDistributedFileSystem
extends|extends
name|DistributedFileSystem
block|{
comment|/**      * Close status of a file. Copied over from HDFS-4525      * @return true if file is already closed      **/
annotation|@
name|Override
specifier|public
name|boolean
name|isFileClosed
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

