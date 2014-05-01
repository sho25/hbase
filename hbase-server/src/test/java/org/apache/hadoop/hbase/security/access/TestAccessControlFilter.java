begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|access
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
name|assertFalse
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
name|PrivilegedExceptionAction
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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|LargeTests
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
name|TableName
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
name|client
operator|.
name|Result
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
name|ResultScanner
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
name|Scan
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
name|User
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

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestAccessControlFilter
extends|extends
name|SecureTestUtil
block|{
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
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|static
name|User
name|READER
decl_stmt|;
specifier|private
specifier|static
name|User
name|LIMITED
decl_stmt|;
specifier|private
specifier|static
name|User
name|DENIED
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|PRIVATE_COL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"private"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|PUBLIC_COL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"public"
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|TABLE
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
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
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|enableSecurity
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|verifyConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// We expect 0.98 scanning semantics
name|conf
operator|.
name|setBoolean
argument_list|(
name|AccessControlConstants
operator|.
name|CF_ATTRIBUTE_EARLY_OUT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
name|READER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"reader"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|LIMITED
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"limited"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|DENIED
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"denied"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
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
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQualifierAccess
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
try|try
block|{
name|doQualifierAccess
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doQualifierAccess
parameter_list|(
specifier|final
name|HTable
name|table
parameter_list|)
throws|throws
name|Exception
block|{
comment|// set permissions
name|SecureTestUtil
operator|.
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|READER
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TABLE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|)
expr_stmt|;
name|SecureTestUtil
operator|.
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|LIMITED
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TABLE
argument_list|,
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|)
expr_stmt|;
comment|// put some test data
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|(
literal|100
argument_list|)
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|PRIVATE_COL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"secret "
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"info "
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
comment|// test read
name|READER
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE
argument_list|)
decl_stmt|;
try|try
block|{
name|ResultScanner
name|rs
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|rowcnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|rs
control|)
block|{
name|rowcnt
operator|++
expr_stmt|;
name|int
name|rownum
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|FAMILY
argument_list|,
name|PRIVATE_COL
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"secret "
operator|+
name|rownum
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|PRIVATE_COL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"info "
operator|+
name|rownum
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Expected 100 rows returned"
argument_list|,
literal|100
argument_list|,
name|rowcnt
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// test read with qualifier filter
name|LIMITED
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE
argument_list|)
decl_stmt|;
try|try
block|{
name|ResultScanner
name|rs
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|rowcnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|rs
control|)
block|{
name|rowcnt
operator|++
expr_stmt|;
name|int
name|rownum
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|FAMILY
argument_list|,
name|PRIVATE_COL
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"info "
operator|+
name|rownum
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Expected 100 rows returned"
argument_list|,
literal|100
argument_list|,
name|rowcnt
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// test as user with no permission
name|DENIED
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE
argument_list|)
decl_stmt|;
try|try
block|{
name|ResultScanner
name|rs
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|rowcnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|rs
control|)
block|{
name|rowcnt
operator|++
expr_stmt|;
name|int
name|rownum
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|FAMILY
argument_list|,
name|PRIVATE_COL
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"info "
operator|+
name|rownum
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|PUBLIC_COL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Expected 0 rows returned"
argument_list|,
literal|0
argument_list|,
name|rowcnt
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

