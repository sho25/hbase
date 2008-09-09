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
package|;
end_package

begin_comment
comment|/**  * Contains the HRegionInfo for the region and the HServerAddress for the  * HRegionServer serving the region  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
class|class
name|HRegionLocation
implements|implements
name|Comparable
block|{
specifier|private
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
name|HServerAddress
name|serverAddress
decl_stmt|;
comment|/**    * Constructor    *     * @param regionInfo the HRegionInfo for the region    * @param serverAddress the HServerAddress for the region server    */
specifier|public
name|HRegionLocation
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|HServerAddress
name|serverAddress
parameter_list|)
block|{
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|serverAddress
operator|=
name|serverAddress
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"address: "
operator|+
name|this
operator|.
name|serverAddress
operator|.
name|toString
argument_list|()
operator|+
literal|", regioninfo: "
operator|+
name|this
operator|.
name|regionInfo
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|this
operator|.
name|compareTo
argument_list|(
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|this
operator|.
name|regionInfo
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|this
operator|.
name|serverAddress
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/** @return HRegionInfo */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|regionInfo
return|;
block|}
comment|/** @return HServerAddress */
specifier|public
name|HServerAddress
name|getServerAddress
parameter_list|()
block|{
return|return
name|serverAddress
return|;
block|}
comment|//
comment|// Comparable
comment|//
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|HRegionLocation
name|other
init|=
operator|(
name|HRegionLocation
operator|)
name|o
decl_stmt|;
name|int
name|result
init|=
name|this
operator|.
name|regionInfo
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|regionInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
name|result
operator|=
name|this
operator|.
name|serverAddress
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|serverAddress
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

