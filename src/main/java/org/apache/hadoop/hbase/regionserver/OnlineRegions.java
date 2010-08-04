begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HRegionInfo
import|;
end_import

begin_comment
comment|/**  * Add and remove online regions.  */
end_comment

begin_interface
interface|interface
name|OnlineRegions
block|{
comment|/**    * Add to online regions.    * @param r    */
name|void
name|addToOnlineRegions
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|)
function_decl|;
comment|/**    * This method removes HRegion corresponding to hri from the Map of onlineRegions.    *    * @param hri the HRegionInfo corresponding to the HRegion to-be-removed.    * @return the removed HRegion, or null if the HRegion was not in onlineRegions.    */
name|HRegion
name|removeFromOnlineRegions
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

