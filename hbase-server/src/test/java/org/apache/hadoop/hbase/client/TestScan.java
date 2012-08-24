begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|hbase
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
name|SmallTests
operator|.
name|class
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

