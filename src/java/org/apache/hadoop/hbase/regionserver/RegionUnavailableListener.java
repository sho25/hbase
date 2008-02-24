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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Used as a callback mechanism so that an HRegion can notify the HRegionServer  * of the different stages making an HRegion unavailable.  Regions are made  * unavailable during region split operations.  */
end_comment

begin_interface
specifier|public
interface|interface
name|RegionUnavailableListener
block|{
comment|/**    *<code>regionName</code> is closing.    * Listener should stop accepting new writes but can continue to service    * outstanding transactions.    * @param regionName    */
specifier|public
name|void
name|closing
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|)
function_decl|;
comment|/**    *<code>regionName</code> is closed and no longer available.    * Listener should clean up any references to<code>regionName</code>    * @param regionName    */
specifier|public
name|void
name|closed
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

