begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
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
name|*
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
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

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
name|TestUser
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestUser
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testBasicAttributes
parameter_list|()
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
name|User
name|user
init|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"simple"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Username should match"
argument_list|,
literal|"simple"
argument_list|,
name|user
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Short username should match"
argument_list|,
literal|"simple"
argument_list|,
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
comment|// don't test shortening of kerberos names because regular Hadoop doesn't support them
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRunAs
parameter_list|()
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
specifier|final
name|User
name|user
init|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"testuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|}
argument_list|)
decl_stmt|;
specifier|final
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|User
name|u
init|=
name|User
operator|.
name|getCurrent
argument_list|()
decl_stmt|;
return|return
name|u
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|String
name|username
init|=
name|user
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Current user within runAs() should match"
argument_list|,
literal|"testuser"
argument_list|,
name|username
argument_list|)
expr_stmt|;
comment|// ensure the next run is correctly set
name|User
name|user2
init|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"testuser2"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|}
argument_list|)
decl_stmt|;
name|String
name|username2
init|=
name|user2
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Second username should match second user"
argument_list|,
literal|"testuser2"
argument_list|,
name|username2
argument_list|)
expr_stmt|;
comment|// check the exception version
name|username
operator|=
name|user
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"User name in runAs() should match"
argument_list|,
literal|"testuser"
argument_list|,
name|username
argument_list|)
expr_stmt|;
comment|// verify that nested contexts work
name|user2
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|nestedName
init|=
name|user
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Nest name should match nested user"
argument_list|,
literal|"testuser"
argument_list|,
name|nestedName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Current name should match current user"
argument_list|,
literal|"testuser2"
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure that we're returning a result for the current user.    * Previously getCurrent() was returning null if not initialized on    * non-secure Hadoop variants.    */
annotation|@
name|Test
specifier|public
name|void
name|testGetCurrent
parameter_list|()
throws|throws
name|Exception
block|{
name|User
name|user1
init|=
name|User
operator|.
name|getCurrent
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|user1
operator|.
name|ugi
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"User1 is "
operator|+
name|user1
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|User
name|u
init|=
name|User
operator|.
name|getCurrent
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|u
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|user1
operator|.
name|getName
argument_list|()
argument_list|,
name|u
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

