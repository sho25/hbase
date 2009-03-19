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
name|rest
operator|.
name|descriptors
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
name|io
operator|.
name|Cell
import|;
end_import

begin_import
import|import
name|agilejson
operator|.
name|TOJSON
import|;
end_import

begin_comment
comment|/**  *   */
end_comment

begin_class
specifier|public
class|class
name|RestCell
extends|extends
name|Cell
block|{
name|byte
index|[]
name|name
decl_stmt|;
comment|/**    *     */
specifier|public
name|RestCell
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// TODO Auto-generated constructor stub
block|}
comment|/**    * @param name     * @param cell    */
specifier|public
name|RestCell
parameter_list|(
name|byte
index|[]
name|name
parameter_list|,
name|Cell
name|cell
parameter_list|)
block|{
name|super
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**    * @param value    * @param timestamp    */
specifier|public
name|RestCell
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
comment|// TODO Auto-generated constructor stub
block|}
comment|/**    * @param vals    * @param ts    */
specifier|public
name|RestCell
parameter_list|(
name|byte
index|[]
index|[]
name|vals
parameter_list|,
name|long
index|[]
name|ts
parameter_list|)
block|{
name|super
argument_list|(
name|vals
argument_list|,
name|ts
argument_list|)
expr_stmt|;
comment|// TODO Auto-generated constructor stub
block|}
comment|/**    * @param value    * @param timestamp    */
specifier|public
name|RestCell
parameter_list|(
name|String
name|value
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
comment|// TODO Auto-generated constructor stub
block|}
comment|/**    * @param vals    * @param ts    */
specifier|public
name|RestCell
parameter_list|(
name|String
index|[]
name|vals
parameter_list|,
name|long
index|[]
name|ts
parameter_list|)
block|{
name|super
argument_list|(
name|vals
argument_list|,
name|ts
argument_list|)
expr_stmt|;
comment|// TODO Auto-generated constructor stub
block|}
comment|/**    * @return the name    */
annotation|@
name|TOJSON
argument_list|(
name|base64
operator|=
literal|true
argument_list|)
specifier|public
name|byte
index|[]
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * @param name the name to set    */
specifier|public
name|void
name|setName
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
block|}
end_class

end_unit

