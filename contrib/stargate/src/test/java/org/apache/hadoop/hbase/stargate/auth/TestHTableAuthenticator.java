begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
operator|.
name|auth
package|;
end_package

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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|HBaseAdmin
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Put
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
name|stargate
operator|.
name|MiniClusterTestBase
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

begin_class
specifier|public
class|class
name|TestHTableAuthenticator
extends|extends
name|MiniClusterTestBase
block|{
specifier|static
specifier|final
name|String
name|UNKNOWN_TOKEN
init|=
literal|"00000000000000000000000000000000"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ADMIN_TOKEN
init|=
literal|"e998efffc67c49c6e14921229a51b7b3"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ADMIN_USERNAME
init|=
literal|"testAdmin"
decl_stmt|;
specifier|static
specifier|final
name|String
name|USER_TOKEN
init|=
literal|"da4829144e3a2febd909a6e1b4ed7cfa"
decl_stmt|;
specifier|static
specifier|final
name|String
name|USER_USERNAME
init|=
literal|"testUser"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DISABLED_TOKEN
init|=
literal|"17de5b5db0fd3de0847bd95396f36d92"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DISABLED_USERNAME
init|=
literal|"disabledUser"
decl_stmt|;
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"TestHTableAuthenticator"
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|USER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"user"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|ADMIN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"admin"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|DISABLED
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"disabled"
argument_list|)
decl_stmt|;
name|HTableAuthenticator
name|authenticator
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|USER
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ADMIN_TOKEN
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|USER
argument_list|,
name|NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ADMIN_USERNAME
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|USER
argument_list|,
name|ADMIN
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|USER_TOKEN
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|USER
argument_list|,
name|NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|USER_USERNAME
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|USER
argument_list|,
name|ADMIN
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|DISABLED_TOKEN
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|USER
argument_list|,
name|NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|DISABLED_USERNAME
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|USER
argument_list|,
name|DISABLED
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
name|authenticator
operator|=
operator|new
name|HTableAuthenticator
argument_list|(
name|conf
argument_list|,
name|TABLE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetUserUnknown
parameter_list|()
throws|throws
name|Exception
block|{
name|User
name|user
init|=
name|authenticator
operator|.
name|getUserForToken
argument_list|(
name|UNKNOWN_TOKEN
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetAdminUser
parameter_list|()
throws|throws
name|Exception
block|{
name|User
name|user
init|=
name|authenticator
operator|.
name|getUserForToken
argument_list|(
name|ADMIN_TOKEN
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|user
operator|.
name|getName
argument_list|()
argument_list|,
name|ADMIN_USERNAME
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|user
operator|.
name|isAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|user
operator|.
name|isDisabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetPlainUser
parameter_list|()
throws|throws
name|Exception
block|{
name|User
name|user
init|=
name|authenticator
operator|.
name|getUserForToken
argument_list|(
name|USER_TOKEN
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|user
operator|.
name|getName
argument_list|()
argument_list|,
name|USER_USERNAME
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|user
operator|.
name|isAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|user
operator|.
name|isDisabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetDisabledUser
parameter_list|()
throws|throws
name|Exception
block|{
name|User
name|user
init|=
name|authenticator
operator|.
name|getUserForToken
argument_list|(
name|DISABLED_TOKEN
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|user
operator|.
name|getName
argument_list|()
argument_list|,
name|DISABLED_USERNAME
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|user
operator|.
name|isAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|user
operator|.
name|isDisabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

