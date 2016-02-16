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
name|net
operator|.
name|BindException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|ServerSocketChannel
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
name|Assert
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
name|TestRule
import|;
end_import

begin_comment
comment|/**  * This tests whether ServerSocketChannel works over ipv6, which Zookeeper  * depends on. On Windows Oracle JDK 6, creating a ServerSocketChannel throws  * java.net.SocketException: Address family not supported by protocol family  * exception. It is a known JVM bug, seems to be only resolved for JDK7:  * http://bugs.sun.com/view_bug.do?bug_id=6230761  *  * For this test, we check that whether we are effected by this bug, and if so  * the test ensures that we are running with java.net.preferIPv4Stack=true, so  * that ZK will not fail to bind to ipv6 address using ClientCnxnSocketNIO.  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestIPv6NIOServerSocketChannel
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
name|TestIPv6NIOServerSocketChannel
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|/**    * Creates and binds a regular ServerSocket.    */
specifier|private
name|void
name|bindServerSocket
parameter_list|(
name|InetAddress
name|inetAddr
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|port
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|addr
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|inetAddr
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|ServerSocket
name|serverSocket
init|=
literal|null
decl_stmt|;
try|try
block|{
name|serverSocket
operator|=
operator|new
name|ServerSocket
argument_list|()
expr_stmt|;
name|serverSocket
operator|.
name|bind
argument_list|(
name|addr
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|BindException
name|ex
parameter_list|)
block|{
comment|//continue
block|}
finally|finally
block|{
if|if
condition|(
name|serverSocket
operator|!=
literal|null
condition|)
block|{
name|serverSocket
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Creates a NIO ServerSocketChannel, and gets the ServerSocket from    * there. Then binds the obtained socket.    * This fails on Windows with Oracle JDK1.6.0u33, if the passed InetAddress is a    * IPv6 address. Works on Oracle JDK 1.7.    */
specifier|private
name|void
name|bindNIOServerSocket
parameter_list|(
name|InetAddress
name|inetAddr
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|port
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|addr
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|inetAddr
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|ServerSocketChannel
name|channel
init|=
literal|null
decl_stmt|;
name|ServerSocket
name|serverSocket
init|=
literal|null
decl_stmt|;
try|try
block|{
name|channel
operator|=
name|ServerSocketChannel
operator|.
name|open
argument_list|()
expr_stmt|;
name|serverSocket
operator|=
name|channel
operator|.
name|socket
argument_list|()
expr_stmt|;
name|serverSocket
operator|.
name|bind
argument_list|(
name|addr
argument_list|)
expr_stmt|;
comment|// This does not work
break|break;
block|}
catch|catch
parameter_list|(
name|BindException
name|ex
parameter_list|)
block|{
comment|//continue
block|}
finally|finally
block|{
if|if
condition|(
name|serverSocket
operator|!=
literal|null
condition|)
block|{
name|serverSocket
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|channel
operator|!=
literal|null
condition|)
block|{
name|channel
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Checks whether we are effected by the JDK issue on windows, and if so    * ensures that we are running with preferIPv4Stack=true.    */
annotation|@
name|Test
specifier|public
name|void
name|testServerSocket
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|addr
init|=
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|1
block|}
decl_stmt|;
name|InetAddress
name|inetAddr
init|=
name|InetAddress
operator|.
name|getByAddress
argument_list|(
name|addr
argument_list|)
decl_stmt|;
try|try
block|{
name|bindServerSocket
argument_list|(
name|inetAddr
argument_list|)
expr_stmt|;
name|bindNIOServerSocket
argument_list|(
name|inetAddr
argument_list|)
expr_stmt|;
comment|//if on *nix or windows JDK7, both will pass
block|}
catch|catch
parameter_list|(
name|java
operator|.
name|net
operator|.
name|SocketException
name|ex
parameter_list|)
block|{
comment|//On Windows JDK6, we will get expected exception:
comment|//java.net.SocketException: Address family not supported by protocol family
comment|//or java.net.SocketException: Protocol family not supported
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ex
operator|.
name|getClass
argument_list|()
operator|.
name|isInstance
argument_list|(
name|BindException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|contains
argument_list|(
literal|"protocol family"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Received expected exception:"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|ex
argument_list|)
expr_stmt|;
comment|//if this is the case, ensure that we are running on preferIPv4=true
name|ensurePreferIPv4
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Checks whether we are running with java.net.preferIPv4Stack=true    */
specifier|public
name|void
name|ensurePreferIPv4
parameter_list|()
throws|throws
name|IOException
block|{
name|InetAddress
index|[]
name|addrs
init|=
name|InetAddress
operator|.
name|getAllByName
argument_list|(
literal|"localhost"
argument_list|)
decl_stmt|;
for|for
control|(
name|InetAddress
name|addr
range|:
name|addrs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"resolved localhost as:"
operator|+
name|addr
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|addr
operator|.
name|getAddress
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|//ensure 4 byte ipv4 address
block|}
block|}
comment|/**    * Tests whether every InetAddress we obtain by resolving can open a    * ServerSocketChannel.    */
annotation|@
name|Test
specifier|public
name|void
name|testServerSocketFromLocalhostResolution
parameter_list|()
throws|throws
name|IOException
block|{
name|InetAddress
index|[]
name|addrs
init|=
name|InetAddress
operator|.
name|getAllByName
argument_list|(
literal|"localhost"
argument_list|)
decl_stmt|;
for|for
control|(
name|InetAddress
name|addr
range|:
name|addrs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"resolved localhost as:"
operator|+
name|addr
argument_list|)
expr_stmt|;
name|bindServerSocket
argument_list|(
name|addr
argument_list|)
expr_stmt|;
name|bindNIOServerSocket
argument_list|(
name|addr
argument_list|)
expr_stmt|;
block|}
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
name|TestIPv6NIOServerSocketChannel
name|test
init|=
operator|new
name|TestIPv6NIOServerSocketChannel
argument_list|()
decl_stmt|;
name|test
operator|.
name|testServerSocket
argument_list|()
expr_stmt|;
name|test
operator|.
name|testServerSocketFromLocalhostResolution
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

