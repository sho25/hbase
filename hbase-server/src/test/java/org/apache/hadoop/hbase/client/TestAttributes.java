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
name|TestAttributes
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
name|Put
name|put
init|=
operator|new
name|Put
argument_list|()
decl_stmt|;
name|put
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
name|put
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
name|put
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
name|ByteArrayOutputStream
name|byteArrayOutputStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutput
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|byteArrayOutputStream
argument_list|)
decl_stmt|;
name|put
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|Put
name|put2
init|=
operator|new
name|Put
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|put2
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|put2
operator|.
name|readFields
argument_list|(
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|byteArrayOutputStream
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|put2
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
name|put2
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
name|put2
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
name|put2
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
name|put2
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
name|testPutAttributes
parameter_list|()
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|put
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
name|put
operator|.
name|getAttribute
argument_list|(
literal|"absent"
argument_list|)
argument_list|)
expr_stmt|;
name|put
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
name|put
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
name|put
operator|.
name|getAttribute
argument_list|(
literal|"absent"
argument_list|)
argument_list|)
expr_stmt|;
comment|// adding attribute
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|put
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
name|testDeleteAttributes
parameter_list|()
block|{
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|del
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
name|del
operator|.
name|getAttribute
argument_list|(
literal|"absent"
argument_list|)
argument_list|)
expr_stmt|;
name|del
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
name|del
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
name|del
operator|.
name|getAttribute
argument_list|(
literal|"absent"
argument_list|)
argument_list|)
expr_stmt|;
comment|// adding attribute
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|del
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
name|testGetId
parameter_list|()
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
literal|"Make sure id is null if unset"
argument_list|,
name|get
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|get
operator|.
name|setId
argument_list|(
literal|"myId"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"myId"
argument_list|,
name|get
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAppendId
parameter_list|()
block|{
name|Append
name|append
init|=
operator|new
name|Append
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
literal|"Make sure id is null if unset"
argument_list|,
name|append
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|append
operator|.
name|setId
argument_list|(
literal|"myId"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"myId"
argument_list|,
name|append
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteId
parameter_list|()
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
literal|"Make sure id is null if unset"
argument_list|,
name|delete
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|delete
operator|.
name|setId
argument_list|(
literal|"myId"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"myId"
argument_list|,
name|delete
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutId
parameter_list|()
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
literal|"Make sure id is null if unset"
argument_list|,
name|put
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|setId
argument_list|(
literal|"myId"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"myId"
argument_list|,
name|put
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanId
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
name|assertNull
argument_list|(
literal|"Make sure id is null if unset"
argument_list|,
name|scan
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setId
argument_list|(
literal|"myId"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"myId"
argument_list|,
name|scan
operator|.
name|toMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"id"
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

