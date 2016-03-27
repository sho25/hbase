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
name|apache
operator|.
name|hadoop
operator|.
name|hdfs
operator|.
name|DFSConfigKeys
operator|.
name|DFS_CLIENT_SOCKET_TIMEOUT_KEY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hdfs
operator|.
name|DFSConfigKeys
operator|.
name|DFS_DATA_ENCRYPTION_ALGORITHM_KEY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hdfs
operator|.
name|DFSConfigKeys
operator|.
name|DFS_ENCRYPT_DATA_TRANSFER_KEY
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
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
name|http
operator|.
name|ssl
operator|.
name|KeyStoreTestUtil
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
name|HBaseKerberosUtils
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
name|token
operator|.
name|TestGenerateDelegationToken
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
name|DFSConfigKeys
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
name|apache
operator|.
name|hadoop
operator|.
name|http
operator|.
name|HttpConfig
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
name|minikdc
operator|.
name|MiniKdc
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
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
name|AfterClass
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
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoop
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoopGroup
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|nio
operator|.
name|NioEventLoopGroup
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
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
name|TestSaslFanOutOneBlockAsyncDFSOutput
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|DistributedFileSystem
name|FS
decl_stmt|;
specifier|private
specifier|static
name|EventLoopGroup
name|EVENT_LOOP_GROUP
decl_stmt|;
specifier|private
specifier|static
name|int
name|READ_TIMEOUT_MS
init|=
literal|200000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|File
name|KEYTAB_FILE
init|=
operator|new
name|File
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"keytab"
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|MiniKdc
name|KDC
decl_stmt|;
specifier|private
specifier|static
name|String
name|HOST
init|=
literal|"localhost"
decl_stmt|;
specifier|private
specifier|static
name|String
name|USERNAME
decl_stmt|;
specifier|private
specifier|static
name|String
name|PRINCIPAL
decl_stmt|;
specifier|private
specifier|static
name|String
name|HTTP_PRINCIPAL
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Parameter
argument_list|(
literal|0
argument_list|)
specifier|public
name|String
name|protection
decl_stmt|;
annotation|@
name|Parameter
argument_list|(
literal|1
argument_list|)
specifier|public
name|String
name|encryptionAlgorithm
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: protection={0}, encryption={1}"
argument_list|)
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|protection
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|"authentication"
argument_list|,
literal|"integrity"
argument_list|,
literal|"privacy"
argument_list|)
control|)
block|{
for|for
control|(
name|String
name|encryptionAlgorithm
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|,
literal|"3des"
argument_list|,
literal|"rc4"
argument_list|)
control|)
block|{
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|protection
block|,
name|encryptionAlgorithm
block|}
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|params
return|;
block|}
specifier|private
specifier|static
name|void
name|setHdfsSecuredConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
comment|// change XXX_USER_NAME_KEY to XXX_KERBEROS_PRINCIPAL_KEY after we drop support for hadoop-2.4.1
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_NAMENODE_USER_NAME_KEY
argument_list|,
name|PRINCIPAL
operator|+
literal|"@"
operator|+
name|KDC
operator|.
name|getRealm
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_NAMENODE_KEYTAB_FILE_KEY
argument_list|,
name|KEYTAB_FILE
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_DATANODE_USER_NAME_KEY
argument_list|,
name|PRINCIPAL
operator|+
literal|"@"
operator|+
name|KDC
operator|.
name|getRealm
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_DATANODE_KEYTAB_FILE_KEY
argument_list|,
name|KEYTAB_FILE
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_WEB_AUTHENTICATION_KERBEROS_PRINCIPAL_KEY
argument_list|,
name|HTTP_PRINCIPAL
operator|+
literal|"@"
operator|+
name|KDC
operator|.
name|getRealm
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_BLOCK_ACCESS_TOKEN_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_HTTP_POLICY_KEY
argument_list|,
name|HttpConfig
operator|.
name|Policy
operator|.
name|HTTPS_ONLY
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_NAMENODE_HTTPS_ADDRESS_KEY
argument_list|,
literal|"localhost:0"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_DATANODE_HTTPS_ADDRESS_KEY
argument_list|,
literal|"localhost:0"
argument_list|)
expr_stmt|;
name|File
name|keystoresDir
init|=
operator|new
name|File
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"keystore"
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|keystoresDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|String
name|sslConfDir
init|=
name|KeyStoreTestUtil
operator|.
name|getClasspathDir
argument_list|(
name|TestGenerateDelegationToken
operator|.
name|class
argument_list|)
decl_stmt|;
name|KeyStoreTestUtil
operator|.
name|setupSSLConfig
argument_list|(
name|keystoresDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|sslConfDir
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"ignore.secure.ports.for.testing"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Logger
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.hdfs.StateChange"
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
literal|"BlockStateChange"
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
name|EVENT_LOOP_GROUP
operator|=
operator|new
name|NioEventLoopGroup
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|DFS_CLIENT_SOCKET_TIMEOUT_KEY
argument_list|,
name|READ_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|Properties
name|conf
init|=
name|MiniKdc
operator|.
name|createConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|put
argument_list|(
name|MiniKdc
operator|.
name|DEBUG
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|KDC
operator|=
operator|new
name|MiniKdc
argument_list|(
name|conf
argument_list|,
operator|new
name|File
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"kdc"
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|KDC
operator|.
name|start
argument_list|()
expr_stmt|;
name|USERNAME
operator|=
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
name|PRINCIPAL
operator|=
name|USERNAME
operator|+
literal|"/"
operator|+
name|HOST
expr_stmt|;
name|HTTP_PRINCIPAL
operator|=
literal|"HTTP/"
operator|+
name|HOST
expr_stmt|;
name|KDC
operator|.
name|createPrincipal
argument_list|(
name|KEYTAB_FILE
argument_list|,
name|PRINCIPAL
argument_list|,
name|HTTP_PRINCIPAL
argument_list|)
expr_stmt|;
name|setHdfsSecuredConfiguration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseKerberosUtils
operator|.
name|setKeytabFileForTesting
argument_list|(
name|KEYTAB_FILE
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseKerberosUtils
operator|.
name|setPrincipalForTesting
argument_list|(
name|PRINCIPAL
operator|+
literal|"@"
operator|+
name|KDC
operator|.
name|getRealm
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseKerberosUtils
operator|.
name|setSecuredConfiguration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UserGroupInformation
operator|.
name|setConfiguration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
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
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|EVENT_LOOP_GROUP
operator|!=
literal|null
condition|)
block|{
name|EVENT_LOOP_GROUP
operator|.
name|shutdownGracefully
argument_list|()
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|KDC
operator|!=
literal|null
condition|)
block|{
name|KDC
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"dfs.data.transfer.protection"
argument_list|,
name|protection
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|encryptionAlgorithm
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|DFS_ENCRYPT_DATA_TRANSFER_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|unset
argument_list|(
name|DFS_DATA_ENCRYPTION_ALGORITHM_KEY
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|DFS_ENCRYPT_DATA_TRANSFER_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|DFS_DATA_ENCRYPTION_ALGORITHM_KEY
argument_list|,
name|encryptionAlgorithm
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|FS
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Path
name|getTestFile
parameter_list|()
block|{
return|return
operator|new
name|Path
argument_list|(
literal|"/"
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"[^0-9a-zA-Z]"
argument_list|,
literal|"_"
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|Path
name|f
init|=
name|getTestFile
argument_list|()
decl_stmt|;
name|EventLoop
name|eventLoop
init|=
name|EVENT_LOOP_GROUP
operator|.
name|next
argument_list|()
decl_stmt|;
specifier|final
name|FanOutOneBlockAsyncDFSOutput
name|out
init|=
name|FanOutOneBlockAsyncDFSOutputHelper
operator|.
name|createOutput
argument_list|(
name|FS
argument_list|,
name|f
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|,
name|FS
operator|.
name|getDefaultBlockSize
argument_list|()
argument_list|,
name|eventLoop
argument_list|)
decl_stmt|;
name|TestFanOutOneBlockAsyncDFSOutput
operator|.
name|writeAndVerify
argument_list|(
name|eventLoop
argument_list|,
name|FS
argument_list|,
name|f
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

