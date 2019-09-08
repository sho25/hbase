begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbtop
operator|.
name|screen
operator|.
name|top
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|hbtop
operator|.
name|field
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Represents headers for the metrics in the top screen.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Header
block|{
specifier|private
specifier|final
name|Field
name|field
decl_stmt|;
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
specifier|public
name|Header
parameter_list|(
name|Field
name|field
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
specifier|public
name|String
name|format
parameter_list|()
block|{
return|return
literal|"%"
operator|+
operator|(
name|field
operator|.
name|isLeftJustify
argument_list|()
condition|?
literal|"-"
else|:
literal|""
operator|)
operator|+
name|length
operator|+
literal|"s"
return|;
block|}
specifier|public
name|Field
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|length
return|;
block|}
block|}
end_class

end_unit
