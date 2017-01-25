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
name|zookeeper
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
name|assertTrue
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
name|FileWriter
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
name|List
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|AppConfigurationEntry
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
name|zookeeper
operator|.
name|ZooDefs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|ACL
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
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
name|TestZooKeeperACL
block|{
specifier|private
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestZooKeeperACL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
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
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|secureZKAvailable
decl_stmt|;
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
name|File
name|saslConfFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"tmp"
argument_list|,
literal|"jaas.conf"
argument_list|)
decl_stmt|;
name|FileWriter
name|fwriter
init|=
operator|new
name|FileWriter
argument_list|(
name|saslConfFile
argument_list|)
decl_stmt|;
name|fwriter
operator|.
name|write
argument_list|(
literal|""
operator|+
literal|"Server {\n"
operator|+
literal|"org.apache.zookeeper.server.auth.DigestLoginModule required\n"
operator|+
literal|"user_hbase=\"secret\";\n"
operator|+
literal|"};\n"
operator|+
literal|"Client {\n"
operator|+
literal|"org.apache.zookeeper.server.auth.DigestLoginModule required\n"
operator|+
literal|"username=\"hbase\"\n"
operator|+
literal|"password=\"secret\";\n"
operator|+
literal|"};"
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|fwriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.security.auth.login.config"
argument_list|,
name|saslConfFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"zookeeper.authProvider.1"
argument_list|,
literal|"org.apache.zookeeper.server.auth.SASLAuthenticationProvider"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.zookeeper.property.maxClientCnxns"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// If Hadoop is missing HADOOP-7070 the cluster will fail to start due to
comment|// the JAAS configuration required by ZK being clobbered by Hadoop
try|try
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Hadoop is missing HADOOP-7070"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|secureZKAvailable
operator|=
literal|false
expr_stmt|;
return|return;
block|}
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|TestZooKeeper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|TEST_UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a node and check its ACL. When authentication is enabled on     * ZooKeeper, all nodes (except /hbase/root-region-server, /hbase/master    * and /hbase/hbaseid) should be created so that only the hbase server user    * (master or region server user) that created them can access them, and    * this user should have all permissions on this node. For    * /hbase/root-region-server, /hbase/master, and /hbase/hbaseid the    * permissions should be as above, but should also be world-readable. First    * we check the general case of /hbase nodes in the following test, and    * then check the subset of world-readable nodes in the three tests after    * that.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testHBaseRootZNodeACL
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|ACL
argument_list|>
name|acls
init|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getACL
argument_list|(
literal|"/hbase"
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
literal|"sasl"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPerms
argument_list|()
argument_list|,
name|ZooDefs
operator|.
name|Perms
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
comment|/**    * When authentication is enabled on ZooKeeper, /hbase/root-region-server    * should be created with 2 ACLs: one specifies that the hbase user has    * full access to the node; the other, that it is world-readable.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testHBaseRootRegionServerZNodeACL
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|ACL
argument_list|>
name|acls
init|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getACL
argument_list|(
literal|"/hbase/root-region-server"
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|boolean
name|foundWorldReadableAcl
init|=
literal|false
decl_stmt|;
name|boolean
name|foundHBaseOwnerAcl
init|=
literal|false
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|acls
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"world"
argument_list|)
operator|==
literal|true
condition|)
block|{
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"anyone"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPerms
argument_list|()
argument_list|,
name|ZooDefs
operator|.
name|Perms
operator|.
name|READ
argument_list|)
expr_stmt|;
name|foundWorldReadableAcl
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|acls
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"sasl"
argument_list|)
operator|==
literal|true
condition|)
block|{
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
literal|"sasl"
argument_list|)
expr_stmt|;
name|foundHBaseOwnerAcl
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// error: should not get here: test fails.
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
name|foundWorldReadableAcl
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundHBaseOwnerAcl
argument_list|)
expr_stmt|;
block|}
comment|/**    * When authentication is enabled on ZooKeeper, /hbase/master should be    * created with 2 ACLs: one specifies that the hbase user has full access    * to the node; the other, that it is world-readable.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testHBaseMasterServerZNodeACL
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|ACL
argument_list|>
name|acls
init|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getACL
argument_list|(
literal|"/hbase/master"
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|boolean
name|foundWorldReadableAcl
init|=
literal|false
decl_stmt|;
name|boolean
name|foundHBaseOwnerAcl
init|=
literal|false
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|acls
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"world"
argument_list|)
operator|==
literal|true
condition|)
block|{
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"anyone"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPerms
argument_list|()
argument_list|,
name|ZooDefs
operator|.
name|Perms
operator|.
name|READ
argument_list|)
expr_stmt|;
name|foundWorldReadableAcl
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|acls
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"sasl"
argument_list|)
operator|==
literal|true
condition|)
block|{
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
literal|"sasl"
argument_list|)
expr_stmt|;
name|foundHBaseOwnerAcl
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// error: should not get here: test fails.
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
name|foundWorldReadableAcl
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundHBaseOwnerAcl
argument_list|)
expr_stmt|;
block|}
comment|/**    * When authentication is enabled on ZooKeeper, /hbase/hbaseid should be    * created with 2 ACLs: one specifies that the hbase user has full access    * to the node; the other, that it is world-readable.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testHBaseIDZNodeACL
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|ACL
argument_list|>
name|acls
init|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getACL
argument_list|(
literal|"/hbase/hbaseid"
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|boolean
name|foundWorldReadableAcl
init|=
literal|false
decl_stmt|;
name|boolean
name|foundHBaseOwnerAcl
init|=
literal|false
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|acls
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"world"
argument_list|)
operator|==
literal|true
condition|)
block|{
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"anyone"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPerms
argument_list|()
argument_list|,
name|ZooDefs
operator|.
name|Perms
operator|.
name|READ
argument_list|)
expr_stmt|;
name|foundWorldReadableAcl
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|acls
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"sasl"
argument_list|)
operator|==
literal|true
condition|)
block|{
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
literal|"sasl"
argument_list|)
expr_stmt|;
name|foundHBaseOwnerAcl
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// error: should not get here: test fails.
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
name|foundWorldReadableAcl
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundHBaseOwnerAcl
argument_list|)
expr_stmt|;
block|}
comment|/**    * Finally, we check the ACLs of a node outside of the /hbase hierarchy and    * verify that its ACL is simply 'hbase:Perms.ALL'.    */
annotation|@
name|Test
specifier|public
name|void
name|testOutsideHBaseNodeACL
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
literal|"/testACLNode"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ACL
argument_list|>
name|acls
init|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getACL
argument_list|(
literal|"/testACLNode"
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
literal|"sasl"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|acls
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPerms
argument_list|()
argument_list|,
name|ZooDefs
operator|.
name|Perms
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check if ZooKeeper JaasConfiguration is valid.    */
annotation|@
name|Test
specifier|public
name|void
name|testIsZooKeeperSecure
parameter_list|()
throws|throws
name|Exception
block|{
name|boolean
name|testJaasConfig
init|=
name|ZKUtil
operator|.
name|isSecureZooKeeper
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|testJaasConfig
argument_list|,
name|secureZKAvailable
argument_list|)
expr_stmt|;
comment|// Define Jaas configuration without ZooKeeper Jaas config
name|File
name|saslConfFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"tmp"
argument_list|,
literal|"fakeJaas.conf"
argument_list|)
decl_stmt|;
name|FileWriter
name|fwriter
init|=
operator|new
name|FileWriter
argument_list|(
name|saslConfFile
argument_list|)
decl_stmt|;
name|fwriter
operator|.
name|write
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|fwriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.security.auth.login.config"
argument_list|,
name|saslConfFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|testJaasConfig
operator|=
name|ZKUtil
operator|.
name|isSecureZooKeeper
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testJaasConfig
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|saslConfFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|/**    * Check if Programmatic way of setting zookeeper security settings is valid.    */
annotation|@
name|Test
specifier|public
name|void
name|testIsZooKeeperSecureWithProgrammaticConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
operator|.
name|setConfiguration
argument_list|(
operator|new
name|DummySecurityConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|Configuration
name|config
init|=
operator|new
name|Configuration
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|testJaasConfig
init|=
name|ZKUtil
operator|.
name|isSecureZooKeeper
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|testJaasConfig
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Now set authentication scheme to Kerberos still it should return false
comment|// because no configuration set
name|config
operator|.
name|set
argument_list|(
literal|"hbase.security.authentication"
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|testJaasConfig
operator|=
name|ZKUtil
operator|.
name|isSecureZooKeeper
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testJaasConfig
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Now set programmatic options related to security
name|config
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZK_CLIENT_KEYTAB_FILE
argument_list|,
literal|"/dummy/file"
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZK_CLIENT_KERBEROS_PRINCIPAL
argument_list|,
literal|"dummy"
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZK_SERVER_KEYTAB_FILE
argument_list|,
literal|"/dummy/file"
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZK_SERVER_KERBEROS_PRINCIPAL
argument_list|,
literal|"dummy"
argument_list|)
expr_stmt|;
name|testJaasConfig
operator|=
name|ZKUtil
operator|.
name|isSecureZooKeeper
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|testJaasConfig
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|DummySecurityConfiguration
extends|extends
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
block|{
annotation|@
name|Override
specifier|public
name|AppConfigurationEntry
index|[]
name|getAppConfigurationEntry
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testAdminDrainAllowedOnSecureZK
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secureZKAvailable
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|drainingServers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|drainingServers
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|parseServerName
argument_list|(
literal|"ZZZ,123,123"
argument_list|)
argument_list|)
expr_stmt|;
comment|// If unable to connect to secure ZK cluster then this operation would fail.
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|drainRegionServers
argument_list|(
name|drainingServers
argument_list|)
expr_stmt|;
name|drainingServers
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|listDrainingRegionServers
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|drainingServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ServerName
operator|.
name|parseServerName
argument_list|(
literal|"ZZZ,123,123"
argument_list|)
argument_list|,
name|drainingServers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|removeDrainFromRegionServers
argument_list|(
name|drainingServers
argument_list|)
expr_stmt|;
name|drainingServers
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|listDrainingRegionServers
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|drainingServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

