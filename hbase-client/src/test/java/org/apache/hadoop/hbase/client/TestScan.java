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
name|client
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
name|fail
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|HConstants
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
name|filter
operator|.
name|FilterList
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
name|visibility
operator|.
name|Authorizations
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|ClientTests
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
name|Assert
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

begin_comment
comment|// TODO: cover more test cases
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
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
name|TestScan
block|{
annotation|@
name|Test
specifier|public
name|void
name|testAttributesSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute1"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute2"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute3"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value3"
argument_list|)
argument_list|)
expr_stmt|;
name|ClientProtos
operator|.
name|Scan
name|scanProto
init|=
name|ProtobufUtil
operator|.
name|toScan
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Scan
name|scan2
init|=
name|ProtobufUtil
operator|.
name|toScan
argument_list|(
name|scanProto
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan2
operator|.
name|getAttribute
argument_list|(
literal|"absent"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
argument_list|,
name|scan2
operator|.
name|getAttribute
argument_list|(
literal|"attribute1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
argument_list|,
name|scan2
operator|.
name|getAttribute
argument_list|(
literal|"attribute2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value3"
argument_list|)
argument_list|,
name|scan2
operator|.
name|getAttribute
argument_list|(
literal|"attribute3"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|scan2
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetToScan
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|get
operator|.
name|setCacheBlocks
argument_list|(
literal|true
argument_list|)
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
operator|.
name|setFilter
argument_list|(
operator|new
name|FilterList
argument_list|()
argument_list|)
operator|.
name|setId
argument_list|(
literal|"get"
argument_list|)
operator|.
name|setIsolationLevel
argument_list|(
name|IsolationLevel
operator|.
name|READ_COMMITTED
argument_list|)
operator|.
name|setLoadColumnFamiliesOnDemand
argument_list|(
literal|false
argument_list|)
operator|.
name|setMaxResultsPerColumnFamily
argument_list|(
literal|1000
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|9999
argument_list|)
operator|.
name|setRowOffsetPerColumnFamily
argument_list|(
literal|5
argument_list|)
operator|.
name|setTimeRange
argument_list|(
literal|0
argument_list|,
literal|13
argument_list|)
operator|.
name|setAttribute
argument_list|(
literal|"att_v0"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"att_v0"
argument_list|)
argument_list|)
operator|.
name|setColumnFamilyTimeRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|123
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getCacheBlocks
argument_list|()
argument_list|,
name|scan
operator|.
name|getCacheBlocks
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getConsistency
argument_list|()
argument_list|,
name|scan
operator|.
name|getConsistency
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getFilter
argument_list|()
argument_list|,
name|scan
operator|.
name|getFilter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getId
argument_list|()
argument_list|,
name|scan
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getIsolationLevel
argument_list|()
argument_list|,
name|scan
operator|.
name|getIsolationLevel
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getLoadColumnFamiliesOnDemandValue
argument_list|()
argument_list|,
name|scan
operator|.
name|getLoadColumnFamiliesOnDemandValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getMaxResultsPerColumnFamily
argument_list|()
argument_list|,
name|scan
operator|.
name|getMaxResultsPerColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|scan
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getRowOffsetPerColumnFamily
argument_list|()
argument_list|,
name|scan
operator|.
name|getRowOffsetPerColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMin
argument_list|()
argument_list|,
name|scan
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|,
name|scan
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|get
operator|.
name|getAttribute
argument_list|(
literal|"att_v0"
argument_list|)
argument_list|,
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"att_v0"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getColumnFamilyTimeRange
argument_list|()
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|)
operator|.
name|getMin
argument_list|()
argument_list|,
name|scan
operator|.
name|getColumnFamilyTimeRange
argument_list|()
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|)
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|get
operator|.
name|getColumnFamilyTimeRange
argument_list|()
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|)
operator|.
name|getMax
argument_list|()
argument_list|,
name|scan
operator|.
name|getColumnFamilyTimeRange
argument_list|()
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|)
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanAttributes
parameter_list|()
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"absent"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"absent"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"absent"
argument_list|)
argument_list|)
expr_stmt|;
comment|// adding attribute
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute1"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
argument_list|,
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"attribute1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"attribute1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// overriding attribute value
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute1"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value12"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value12"
argument_list|)
argument_list|,
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"attribute1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value12"
argument_list|)
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"attribute1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// adding another attribute
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute2"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
argument_list|,
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"attribute2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"attribute2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// removing attribute
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute2"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"attribute2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"attribute2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// removing non-existed attribute
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute2"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"attribute2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"attribute2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// removing another attribute
name|scan
operator|.
name|setAttribute
argument_list|(
literal|"attribute1"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttribute
argument_list|(
literal|"attribute1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"attribute1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNullQualifier
parameter_list|()
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|qualifiers
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|qualifiers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetAuthorizations
parameter_list|()
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
try|try
block|{
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"\u002b|\u0029"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"A"
argument_list|,
literal|"B"
argument_list|,
literal|"0123"
argument_list|,
literal|"A0"
argument_list|,
literal|"1A1"
argument_list|,
literal|"_a"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"A|B"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"A&B"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"!B"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"A"
argument_list|,
literal|"(A)"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"A"
argument_list|,
literal|"{A"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|" "
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|":B"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"-B"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|".B"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"/B"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"should not throw exception"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetStartRowAndSetStopRow
parameter_list|()
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
operator|new
name|byte
index|[
name|HConstants
operator|.
name|MAX_ROW_LENGTH
index|]
argument_list|)
expr_stmt|;
try|try
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
operator|new
name|byte
index|[
name|HConstants
operator|.
name|MAX_ROW_LENGTH
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should've thrown exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{     }
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"expected IllegalArgumentException to be thrown"
argument_list|)
expr_stmt|;
block|}
name|scan
operator|.
name|setStopRow
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStopRow
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStopRow
argument_list|(
operator|new
name|byte
index|[
name|HConstants
operator|.
name|MAX_ROW_LENGTH
index|]
argument_list|)
expr_stmt|;
try|try
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
operator|new
name|byte
index|[
name|HConstants
operator|.
name|MAX_ROW_LENGTH
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should've thrown exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{     }
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"expected IllegalArgumentException to be thrown"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

