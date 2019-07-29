begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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
name|HBaseClassTestRule
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
name|ClassRule
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
name|ExpectedException
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestStrings
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|ExpectedException
name|thrown
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestStrings
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testAppendKeyValue
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"foo, bar=baz"
argument_list|,
name|Strings
operator|.
name|appendKeyValue
argument_list|(
operator|new
name|StringBuilder
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"bar->baz"
argument_list|,
name|Strings
operator|.
name|appendKeyValue
argument_list|(
operator|new
name|StringBuilder
argument_list|()
argument_list|,
literal|"bar"
argument_list|,
literal|"baz"
argument_list|,
literal|"->"
argument_list|,
literal|"| "
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"foo, bar=baz"
argument_list|,
name|Strings
operator|.
name|appendKeyValue
argument_list|(
operator|new
name|StringBuilder
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"bar"
argument_list|,
literal|"baz"
argument_list|,
literal|"="
argument_list|,
literal|", "
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"foo| bar->baz"
argument_list|,
name|Strings
operator|.
name|appendKeyValue
argument_list|(
operator|new
name|StringBuilder
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"bar"
argument_list|,
literal|"baz"
argument_list|,
literal|"->"
argument_list|,
literal|"| "
argument_list|)
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
name|testDomainNamePointerToHostName
parameter_list|()
block|{
name|Assert
operator|.
name|assertNull
argument_list|(
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"foo.com"
argument_list|,
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
literal|"foo.com"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"foo.bar.com"
argument_list|,
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
literal|"foo.bar.com"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"foo.bar.com"
argument_list|,
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
literal|"foo.bar.com."
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPadFront
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"ddfoo"
argument_list|,
name|Strings
operator|.
name|padFront
argument_list|(
literal|"foo"
argument_list|,
literal|'d'
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|thrown
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|padFront
argument_list|(
literal|"foo"
argument_list|,
literal|'d'
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
