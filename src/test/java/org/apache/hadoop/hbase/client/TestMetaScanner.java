begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

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

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
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
name|TestMetaScanner
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
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
argument_list|(
literal|1
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
name|testMetaScanner
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting testMetaScanner"
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|TABLENAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testMetaScanner"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLENAME
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|conf
argument_list|,
name|table
argument_list|,
name|FAMILY
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|HConstants
operator|.
name|EMPTY_START_ROW
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"region_a"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"region_b"
argument_list|)
block|}
argument_list|)
expr_stmt|;
comment|// Make sure all the regions are deployed
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|MetaScanner
operator|.
name|MetaScannerVisitor
name|visitor
init|=
name|mock
argument_list|(
name|MetaScanner
operator|.
name|MetaScannerVisitor
operator|.
name|class
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|visitor
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
comment|// Scanning the entire table should give us three rows
name|MetaScanner
operator|.
name|metaScan
argument_list|(
name|conf
argument_list|,
name|visitor
argument_list|,
name|TABLENAME
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|visitor
argument_list|,
name|times
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
comment|// Scanning the table with a specified empty start row should also
comment|// give us three META rows
name|reset
argument_list|(
name|visitor
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|visitor
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
name|MetaScanner
operator|.
name|metaScan
argument_list|(
name|conf
argument_list|,
name|visitor
argument_list|,
name|TABLENAME
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|visitor
argument_list|,
name|times
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
comment|// Scanning the table starting in the middle should give us two rows:
comment|// region_a and region_b
name|reset
argument_list|(
name|visitor
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|visitor
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
name|MetaScanner
operator|.
name|metaScan
argument_list|(
name|conf
argument_list|,
name|visitor
argument_list|,
name|TABLENAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"region_ac"
argument_list|)
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|visitor
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
comment|// Scanning with a limit of 1 should only give us one row
name|reset
argument_list|(
name|visitor
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|visitor
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
name|MetaScanner
operator|.
name|metaScan
argument_list|(
name|conf
argument_list|,
name|visitor
argument_list|,
name|TABLENAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"region_ac"
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|visitor
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|processRow
argument_list|(
operator|(
name|Result
operator|)
name|anyObject
argument_list|()
argument_list|)
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

