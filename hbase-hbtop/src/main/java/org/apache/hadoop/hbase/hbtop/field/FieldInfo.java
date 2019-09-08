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
name|field
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Information about a field.  *  * This has a {@link Field} itself and additional information (e.g. {@code defaultLength} and  * {@code displayByDefault}). This additional information is different between the  * {@link org.apache.hadoop.hbase.hbtop.mode.Mode}s even when the field is the same. That's why the  * additional information is separated from {@link Field}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FieldInfo
block|{
specifier|private
specifier|final
name|Field
name|field
decl_stmt|;
specifier|private
specifier|final
name|int
name|defaultLength
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|displayByDefault
decl_stmt|;
specifier|public
name|FieldInfo
parameter_list|(
name|Field
name|field
parameter_list|,
name|int
name|defaultLength
parameter_list|,
name|boolean
name|displayByDefault
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
name|defaultLength
operator|=
name|defaultLength
expr_stmt|;
name|this
operator|.
name|displayByDefault
operator|=
name|displayByDefault
expr_stmt|;
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
name|getDefaultLength
parameter_list|()
block|{
return|return
name|defaultLength
return|;
block|}
specifier|public
name|boolean
name|isDisplayByDefault
parameter_list|()
block|{
return|return
name|displayByDefault
return|;
block|}
block|}
end_class

end_unit
