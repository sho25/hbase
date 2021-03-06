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
name|client
operator|.
name|hamcrest
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|Is
operator|.
name|is
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
name|hamcrest
operator|.
name|Description
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|TypeSafeDiagnosingMatcher
import|;
end_import

begin_comment
comment|/**  * Helper methods for matching against values passed through the helper methods of {@link Bytes}.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|BytesMatchers
block|{
specifier|private
name|BytesMatchers
parameter_list|()
block|{}
specifier|public
specifier|static
name|Matcher
argument_list|<
name|byte
index|[]
argument_list|>
name|bytesAsStringBinary
parameter_list|(
specifier|final
name|String
name|binary
parameter_list|)
block|{
return|return
name|bytesAsStringBinary
argument_list|(
name|is
argument_list|(
name|binary
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Matcher
argument_list|<
name|byte
index|[]
argument_list|>
name|bytesAsStringBinary
parameter_list|(
specifier|final
name|Matcher
argument_list|<
name|String
argument_list|>
name|matcher
parameter_list|)
block|{
return|return
operator|new
name|TypeSafeDiagnosingMatcher
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|matchesSafely
parameter_list|(
name|byte
index|[]
name|item
parameter_list|,
name|Description
name|mismatchDescription
parameter_list|)
block|{
specifier|final
name|String
name|binary
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|item
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|(
name|binary
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|mismatchDescription
operator|.
name|appendText
argument_list|(
literal|"was a byte[] with a Bytes.toStringBinary value "
argument_list|)
expr_stmt|;
name|matcher
operator|.
name|describeMismatch
argument_list|(
name|binary
argument_list|,
name|mismatchDescription
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|describeTo
parameter_list|(
name|Description
name|description
parameter_list|)
block|{
name|description
operator|.
name|appendText
argument_list|(
literal|"has a byte[] with a Bytes.toStringBinary value that "
argument_list|)
operator|.
name|appendDescriptionOf
argument_list|(
name|matcher
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

