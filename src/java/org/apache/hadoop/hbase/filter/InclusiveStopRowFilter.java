begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Subclass of StopRowFilter that filters rows> the stop row,  * making it include up to the last row but no further.  */
end_comment

begin_class
specifier|public
class|class
name|InclusiveStopRowFilter
extends|extends
name|StopRowFilter
block|{
comment|/**    * Default constructor, filters nothing. Required though for RPC    * deserialization.    */
specifier|public
name|InclusiveStopRowFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that takes a stopRowKey on which to filter    *     * @param stopRowKey rowKey to filter on.    */
specifier|public
name|InclusiveStopRowFilter
parameter_list|(
specifier|final
name|Text
name|stopRowKey
parameter_list|)
block|{
name|super
argument_list|(
name|stopRowKey
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
specifier|final
name|Text
name|rowKey
parameter_list|)
block|{
if|if
condition|(
name|rowKey
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|stopRowKey
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
return|return
name|this
operator|.
name|stopRowKey
operator|.
name|compareTo
argument_list|(
name|rowKey
argument_list|)
operator|<
literal|0
return|;
block|}
block|}
end_class

end_unit

