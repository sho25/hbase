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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * HServerInfo contains metainfo about an HRegionServer, Currently it only  * contains the server start code.  *   * In the future it will contain information about the source machine and  * load statistics.  */
end_comment

begin_class
specifier|public
class|class
name|HServerInfo
implements|implements
name|Writable
block|{
specifier|private
name|HServerAddress
name|serverAddress
decl_stmt|;
specifier|private
name|long
name|startCode
decl_stmt|;
specifier|private
name|HServerLoad
name|load
decl_stmt|;
specifier|private
name|int
name|infoPort
decl_stmt|;
comment|/** default constructor - used by Writable */
specifier|public
name|HServerInfo
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|HServerAddress
argument_list|()
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_INFOPORT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param serverAddress    * @param startCode    * @param infoPort Port the info server is listening on.    */
specifier|public
name|HServerInfo
parameter_list|(
name|HServerAddress
name|serverAddress
parameter_list|,
name|long
name|startCode
parameter_list|,
specifier|final
name|int
name|infoPort
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
name|serverAddress
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|startCode
expr_stmt|;
name|this
operator|.
name|load
operator|=
operator|new
name|HServerLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|infoPort
expr_stmt|;
block|}
comment|/**    * Construct a new object using another as input (like a copy constructor)    * @param other    */
specifier|public
name|HServerInfo
parameter_list|(
name|HServerInfo
name|other
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
operator|new
name|HServerAddress
argument_list|(
name|other
operator|.
name|getServerAddress
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|other
operator|.
name|getStartCode
argument_list|()
expr_stmt|;
name|this
operator|.
name|load
operator|=
name|other
operator|.
name|getLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|other
operator|.
name|getInfoPort
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the load    */
specifier|public
name|HServerLoad
name|getLoad
parameter_list|()
block|{
return|return
name|load
return|;
block|}
comment|/**    * @param load the load to set    */
specifier|public
name|void
name|setLoad
parameter_list|(
name|HServerLoad
name|load
parameter_list|)
block|{
name|this
operator|.
name|load
operator|=
name|load
expr_stmt|;
block|}
comment|/** @return the server address */
specifier|public
name|HServerAddress
name|getServerAddress
parameter_list|()
block|{
return|return
name|serverAddress
return|;
block|}
comment|/** @return the server start code */
specifier|public
name|long
name|getStartCode
parameter_list|()
block|{
return|return
name|startCode
return|;
block|}
comment|/**    * @return Port the info server is listening on.    */
specifier|public
name|int
name|getInfoPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|infoPort
return|;
block|}
comment|/**    * @param startCode the startCode to set    */
specifier|public
name|void
name|setStartCode
parameter_list|(
name|long
name|startCode
parameter_list|)
block|{
name|this
operator|.
name|startCode
operator|=
name|startCode
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
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
operator|+
literal|", startcode: "
operator|+
name|this
operator|.
name|startCode
operator|+
literal|", load: ("
operator|+
name|this
operator|.
name|load
operator|.
name|toString
argument_list|()
operator|+
literal|")"
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|HServerInfo
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HServerInfo
name|that
init|=
operator|(
name|HServerInfo
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|serverAddress
operator|.
name|equals
argument_list|(
name|that
operator|.
name|serverAddress
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|.
name|infoPort
operator|!=
name|that
operator|.
name|infoPort
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|.
name|startCode
operator|!=
name|that
operator|.
name|startCode
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
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
name|serverAddress
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|this
operator|.
name|infoPort
expr_stmt|;
name|result
operator|^=
name|this
operator|.
name|startCode
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Writable
comment|/** {@inheritDoc} */
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|serverAddress
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|load
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|serverAddress
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|startCode
argument_list|)
expr_stmt|;
name|this
operator|.
name|load
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|infoPort
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

