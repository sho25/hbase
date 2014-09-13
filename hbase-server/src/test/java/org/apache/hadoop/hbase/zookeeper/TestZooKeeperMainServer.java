begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|Permission
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
name|hbase
operator|.
name|testclassification
operator|.
name|SmallTests
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestZooKeeperMainServer
block|{
comment|// ZKMS calls System.exit.  Catch the call and prevent exit using trick described up in
comment|// http://stackoverflow.com/questions/309396/java-how-to-test-methods-that-call-system-exit
specifier|protected
specifier|static
class|class
name|ExitException
extends|extends
name|SecurityException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|final
name|int
name|status
decl_stmt|;
specifier|public
name|ExitException
parameter_list|(
name|int
name|status
parameter_list|)
block|{
name|super
argument_list|(
literal|"There is no escape!"
argument_list|)
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|NoExitSecurityManager
extends|extends
name|SecurityManager
block|{
annotation|@
name|Override
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|)
block|{
comment|// allow anything.
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|,
name|Object
name|context
parameter_list|)
block|{
comment|// allow anything.
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkExit
parameter_list|(
name|int
name|status
parameter_list|)
block|{
name|super
operator|.
name|checkExit
argument_list|(
name|status
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ExitException
argument_list|(
name|status
argument_list|)
throw|;
block|}
block|}
comment|/**    * We need delete of a znode to work at least.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testCommandLineWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|NoExitSecurityManager
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|htu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|htu
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
try|try
block|{
name|ZooKeeperWatcher
name|zkw
init|=
name|htu
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|String
name|znode
init|=
literal|"/testCommandLineWorks"
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
expr_stmt|;
name|boolean
name|exception
init|=
literal|false
decl_stmt|;
try|try
block|{
name|ZooKeeperMainServer
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-server"
block|,
literal|"localhost:"
operator|+
name|htu
operator|.
name|getZkCluster
argument_list|()
operator|.
name|getClientPort
argument_list|()
block|,
literal|"delete"
block|,
name|znode
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitException
name|ee
parameter_list|)
block|{
comment|// ZKMS calls System.exit which should trigger this exception.
name|exception
operator|=
literal|true
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|exception
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|htu
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// or save and restore original
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHostPortParse
parameter_list|()
block|{
name|ZooKeeperMainServer
name|parser
init|=
operator|new
name|ZooKeeperMainServer
argument_list|()
decl_stmt|;
name|Configuration
name|c
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"localhost:"
operator|+
name|c
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|)
argument_list|,
name|parser
operator|.
name|parse
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|String
name|port
init|=
literal|"1234"
decl_stmt|;
name|c
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"hbase.zookeeper.quorum"
argument_list|,
literal|"example.com"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"example.com:"
operator|+
name|port
argument_list|,
name|parser
operator|.
name|parse
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"hbase.zookeeper.quorum"
argument_list|,
literal|"example1.com,example2.com,example3.com"
argument_list|)
expr_stmt|;
name|String
name|ensemble
init|=
name|parser
operator|.
name|parse
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|port
argument_list|,
name|ensemble
operator|.
name|matches
argument_list|(
literal|"(example[1-3]\\.com:1234,){2}example[1-3]\\.com:"
operator|+
name|port
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

