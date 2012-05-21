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
name|ipc
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
name|assertNotSame
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
name|InetSocketAddress
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
name|SmallTests
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
name|io
operator|.
name|Text
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
name|io
operator|.
name|Writable
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|DescriptorProtos
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|DescriptorProtos
operator|.
name|EnumDescriptorProto
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

begin_comment
comment|/** Unit tests to test PB-based types on WritableRpcEngine. */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestPBOnWritableRpc
block|{
specifier|private
specifier|static
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|public
interface|interface
name|TestProtocol
extends|extends
name|VersionedProtocol
block|{
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|1L
decl_stmt|;
name|String
name|echo
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|Writable
name|echo
parameter_list|(
name|Writable
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|DescriptorProtos
operator|.
name|EnumDescriptorProto
name|exchangeProto
parameter_list|(
name|DescriptorProtos
operator|.
name|EnumDescriptorProto
name|arg
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
class|class
name|TestImpl
implements|implements
name|TestProtocol
block|{
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|)
block|{
return|return
name|TestProtocol
operator|.
name|VERSION
return|;
block|}
specifier|public
name|ProtocolSignature
name|getProtocolSignature
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|int
name|hashcode
parameter_list|)
block|{
return|return
operator|new
name|ProtocolSignature
argument_list|(
name|TestProtocol
operator|.
name|VERSION
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|echo
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|echo
parameter_list|(
name|Writable
name|writable
parameter_list|)
block|{
return|return
name|writable
return|;
block|}
annotation|@
name|Override
specifier|public
name|EnumDescriptorProto
name|exchangeProto
parameter_list|(
name|EnumDescriptorProto
name|arg
parameter_list|)
block|{
return|return
name|arg
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
name|testCalls
parameter_list|()
throws|throws
name|Exception
block|{
name|testCallsInternal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testCallsInternal
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|RpcServer
name|rpcServer
init|=
name|HBaseRPC
operator|.
name|getServer
argument_list|(
operator|new
name|TestImpl
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestProtocol
operator|.
name|class
block|}
operator|,
literal|"localhost"
operator|,
comment|// BindAddress is IP we got for this server.
literal|9999
operator|,
comment|// port number
literal|2
operator|,
comment|// number of handlers
literal|0
operator|,
comment|// we dont use high priority handlers in master
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.rpc.verbose"
argument_list|,
literal|false
argument_list|)
operator|,
name|conf
operator|,
literal|0
block|)
function|;
name|TestProtocol
name|proxy
init|=
literal|null
decl_stmt|;
try|try
block|{
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|InetSocketAddress
name|isa
init|=
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|9999
argument_list|)
decl_stmt|;
name|proxy
operator|=
operator|(
name|TestProtocol
operator|)
name|HBaseRPC
operator|.
name|waitForProxy
argument_list|(
name|TestProtocol
operator|.
name|class
argument_list|,
name|TestProtocol
operator|.
name|VERSION
argument_list|,
name|isa
argument_list|,
name|conf
argument_list|,
operator|-
literal|1
argument_list|,
literal|8000
argument_list|,
literal|8000
argument_list|)
expr_stmt|;
name|String
name|stringResult
init|=
name|proxy
operator|.
name|echo
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|stringResult
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|stringResult
operator|=
name|proxy
operator|.
name|echo
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|stringResult
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Text
name|utf8Result
init|=
operator|(
name|Text
operator|)
name|proxy
operator|.
name|echo
argument_list|(
operator|new
name|Text
argument_list|(
literal|"hello world"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|utf8Result
argument_list|,
operator|new
name|Text
argument_list|(
literal|"hello world"
argument_list|)
argument_list|)
expr_stmt|;
name|utf8Result
operator|=
operator|(
name|Text
operator|)
name|proxy
operator|.
name|echo
argument_list|(
operator|(
name|Text
operator|)
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|utf8Result
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Test protobufs
name|EnumDescriptorProto
name|sendProto
init|=
name|EnumDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"test"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|EnumDescriptorProto
name|retProto
init|=
name|proxy
operator|.
name|exchangeProto
argument_list|(
name|sendProto
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|sendProto
argument_list|,
name|retProto
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|sendProto
argument_list|,
name|retProto
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|rpcServer
operator|.
name|stop
argument_list|()
expr_stmt|;
if|if
condition|(
name|proxy
operator|!=
literal|null
condition|)
block|{
name|HBaseRPC
operator|.
name|stopProxy
argument_list|(
name|proxy
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

begin_function
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
operator|new
name|TestPBOnWritableRpc
argument_list|()
operator|.
name|testCallsInternal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
end_function

begin_decl_stmt
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
end_decl_stmt

unit|}
end_unit

