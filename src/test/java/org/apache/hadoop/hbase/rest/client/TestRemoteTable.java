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
name|rest
operator|.
name|client
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
name|client
operator|.
name|Delete
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
name|Get
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
name|rest
operator|.
name|HBaseRESTTestingUtility
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
name|rest
operator|.
name|client
operator|.
name|Client
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
name|rest
operator|.
name|client
operator|.
name|Cluster
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
name|rest
operator|.
name|client
operator|.
name|RemoteHTable
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
name|TestRemoteTable
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
name|TestRemoteTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"TestRemoteTable"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow4"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testvalue1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testvalue2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|ONE_HOUR
init|=
literal|60
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|TS_2
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|TS_1
init|=
name|TS_2
operator|-
name|ONE_HOUR
decl_stmt|;
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
specifier|final
name|HBaseRESTTestingUtility
name|REST_TEST_UTIL
init|=
operator|new
name|HBaseRESTTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|RemoteHTable
name|remoteTable
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
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
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_2
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_3
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|ROW_1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|TS_2
argument_list|,
name|VALUE_1
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
name|ROW_2
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|TS_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|TS_2
argument_list|,
name|VALUE_2
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|,
name|TS_2
argument_list|,
name|VALUE_2
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
name|remoteTable
operator|=
operator|new
name|RemoteHTable
argument_list|(
operator|new
name|Client
argument_list|(
operator|new
name|Cluster
argument_list|()
operator|.
name|add
argument_list|(
literal|"localhost"
argument_list|,
name|REST_TEST_UTIL
operator|.
name|getServletPort
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLE
argument_list|,
literal|null
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
name|remoteTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|shutdownServletContainer
argument_list|()
expr_stmt|;
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
name|testGetTableDescriptor
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|local
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLE
argument_list|)
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|remoteTable
operator|.
name|getTableDescriptor
argument_list|()
argument_list|,
name|local
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGet
parameter_list|()
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value1
init|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value2
init|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_3
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
comment|// @TS_2
name|assertNotNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
comment|// @TS_2
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
comment|// @TS_2
name|assertNotNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
comment|// test timestamp
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_2
argument_list|)
expr_stmt|;
name|get
operator|.
name|setTimeStamp
argument_list|(
name|TS_1
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
comment|// @TS_1
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
comment|// test timerange
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_2
argument_list|)
expr_stmt|;
name|get
operator|.
name|setTimeRange
argument_list|(
literal|0
argument_list|,
name|TS_1
operator|+
literal|1
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
comment|// @TS_1
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
comment|// test maxVersions
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|setMaxVersions
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|result
operator|.
name|list
argument_list|()
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|COLUMN_1
argument_list|,
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|&&
name|TS_1
operator|==
name|kv
operator|.
name|getTimestamp
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// @TS_1
name|count
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|COLUMN_1
argument_list|,
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|&&
name|TS_2
operator|==
name|kv
operator|.
name|getTimestamp
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// @TS_2
name|count
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPut
parameter_list|()
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_3
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW_3
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
comment|// multiput
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
argument_list|()
decl_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_3
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|,
name|VALUE_2
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_4
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_4
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|,
name|VALUE_2
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_3
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_2
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_4
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|value
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_3
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|,
name|VALUE_2
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW_3
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_2
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value1
init|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value2
init|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_2
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|ROW_3
argument_list|)
decl_stmt|;
name|delete
operator|.
name|deleteColumn
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_3
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_2
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE_1
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
name|delete
operator|=
operator|new
name|Delete
argument_list|(
name|ROW_3
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_3
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|COLUMN_2
argument_list|)
expr_stmt|;
name|result
operator|=
name|remoteTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|value1
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|)
expr_stmt|;
name|value2
operator|=
name|result
operator|.
name|getValue
argument_list|(
name|COLUMN_2
argument_list|,
name|QUALIFIER_2
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value1
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value2
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testScanner
parameter_list|()
throws|throws
name|IOException
block|{
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
argument_list|()
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_3
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_4
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|remoteTable
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|Result
index|[]
name|results
init|=
name|scanner
operator|.
name|next
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|ROW_1
argument_list|,
name|results
index|[
literal|0
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|=
name|scanner
operator|.
name|next
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|ROW_2
argument_list|,
name|results
index|[
literal|0
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|ROW_3
argument_list|,
name|results
index|[
literal|1
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|ROW_4
argument_list|,
name|results
index|[
literal|2
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|=
name|scanner
operator|.
name|next
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
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

