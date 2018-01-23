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
name|regionserver
package|;
end_package

begin_class
specifier|public
class|class
name|SteppingSplitPolicy
extends|extends
name|IncreasingToUpperBoundRegionSplitPolicy
block|{
comment|/**    * @return flushSize * 2 if there's exactly one region of the table in question    * found on this regionserver. Otherwise max file size.    * This allows a table to spread quickly across servers, while avoiding creating    * too many regions.    */
annotation|@
name|Override
specifier|protected
name|long
name|getSizeToCheck
parameter_list|(
specifier|final
name|int
name|tableRegionsCount
parameter_list|)
block|{
return|return
name|tableRegionsCount
operator|==
literal|1
condition|?
name|this
operator|.
name|initialSize
else|:
name|getDesiredMaxFileSize
argument_list|()
return|;
block|}
block|}
end_class

end_unit

