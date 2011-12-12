begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|util
operator|.
name|Writables
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
comment|/**  * Tests for {@link HServerAddress}  */
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
name|TestHServerAddress
block|{
annotation|@
name|Test
specifier|public
name|void
name|testHashCode
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerAddress
name|hsa2
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hsa1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|hsa2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|HServerAddress
name|hsa3
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1235
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|hsa1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|hsa3
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHServerAddress
parameter_list|()
block|{
operator|new
name|HServerAddress
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHServerAddressInetSocketAddress
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|hsa1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHServerAddressString
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerAddress
name|hsa2
init|=
operator|new
name|HServerAddress
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hsa1
operator|.
name|equals
argument_list|(
name|hsa2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHServerAddressHServerAddress
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerAddress
name|hsa2
init|=
operator|new
name|HServerAddress
argument_list|(
name|hsa1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hsa1
argument_list|,
name|hsa2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadFields
parameter_list|()
throws|throws
name|IOException
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerAddress
name|hsa2
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1235
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|hsa1
argument_list|)
decl_stmt|;
name|HServerAddress
name|deserialized
init|=
operator|(
name|HServerAddress
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HServerAddress
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hsa1
argument_list|,
name|deserialized
argument_list|)
expr_stmt|;
name|bytes
operator|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|hsa2
argument_list|)
expr_stmt|;
name|deserialized
operator|=
operator|(
name|HServerAddress
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HServerAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|hsa1
argument_list|,
name|deserialized
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

