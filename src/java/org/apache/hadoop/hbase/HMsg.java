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
comment|/*******************************************************************************  * HMsg is for communicating instructions between the HMaster and the   * HRegionServers.  ******************************************************************************/
end_comment

begin_class
specifier|public
class|class
name|HMsg
implements|implements
name|Writable
block|{
comment|// Messages sent from master to region server
comment|/** Start serving the specified region */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_OPEN
init|=
literal|1
decl_stmt|;
comment|/** Stop serving the specified region */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_CLOSE
init|=
literal|2
decl_stmt|;
comment|/** Region server is unknown to master. Restart */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_CALL_SERVER_STARTUP
init|=
literal|4
decl_stmt|;
comment|/** Master tells region server to stop */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGIONSERVER_STOP
init|=
literal|5
decl_stmt|;
comment|/** Stop serving the specified region and don't report back that it's closed */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_CLOSE_WITHOUT_REPORT
init|=
literal|6
decl_stmt|;
comment|// Messages sent from the region server to the master
comment|/** region server is now serving the specified region */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REPORT_OPEN
init|=
literal|100
decl_stmt|;
comment|/** region server is no longer serving the specified region */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REPORT_CLOSE
init|=
literal|101
decl_stmt|;
comment|/**    * region server split the region associated with this message.    *     * note that this message is immediately followed by two MSG_REPORT_OPEN    * messages, one for each of the new regions resulting from the split    */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REPORT_SPLIT
init|=
literal|103
decl_stmt|;
comment|/**    * region server is shutting down    *     * note that this message is followed by MSG_REPORT_CLOSE messages for each    * region the region server was serving.    */
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REPORT_EXITING
init|=
literal|104
decl_stmt|;
name|byte
name|msg
decl_stmt|;
name|HRegionInfo
name|info
decl_stmt|;
comment|/** Default constructor. Used during deserialization */
specifier|public
name|HMsg
parameter_list|()
block|{
name|this
operator|.
name|info
operator|=
operator|new
name|HRegionInfo
argument_list|()
expr_stmt|;
block|}
comment|/**    * Construct a message with an empty HRegionInfo    *     * @param msg - message code    */
specifier|public
name|HMsg
parameter_list|(
name|byte
name|msg
parameter_list|)
block|{
name|this
operator|.
name|msg
operator|=
name|msg
expr_stmt|;
name|this
operator|.
name|info
operator|=
operator|new
name|HRegionInfo
argument_list|()
expr_stmt|;
block|}
comment|/**    * Construct a message with the specified message code and HRegionInfo    *     * @param msg - message code    * @param info - HRegionInfo    */
specifier|public
name|HMsg
parameter_list|(
name|byte
name|msg
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
block|{
name|this
operator|.
name|msg
operator|=
name|msg
expr_stmt|;
name|this
operator|.
name|info
operator|=
name|info
expr_stmt|;
block|}
comment|/**    * Accessor    * @return message code    */
specifier|public
name|byte
name|getMsg
parameter_list|()
block|{
return|return
name|msg
return|;
block|}
comment|/**    * Accessor    * @return HRegionInfo    */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|info
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|message
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|msg
condition|)
block|{
case|case
name|MSG_REGION_OPEN
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REGION_OPEN : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_REGION_CLOSE
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REGION_CLOSE : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_CALL_SERVER_STARTUP
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_CALL_SERVER_STARTUP : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_REGIONSERVER_STOP
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REGIONSERVER_STOP : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_REGION_CLOSE_WITHOUT_REPORT
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REGION_CLOSE_WITHOUT_REPORT : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_REPORT_OPEN
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REPORT_OPEN : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_REPORT_CLOSE
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REPORT_CLOSE : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_REPORT_SPLIT
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REGION_SPLIT : "
argument_list|)
expr_stmt|;
break|break;
case|case
name|MSG_REPORT_EXITING
case|:
name|message
operator|.
name|append
argument_list|(
literal|"MSG_REPORT_EXITING : "
argument_list|)
expr_stmt|;
break|break;
default|default:
name|message
operator|.
name|append
argument_list|(
literal|"unknown message code ("
argument_list|)
expr_stmt|;
name|message
operator|.
name|append
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|message
operator|.
name|append
argument_list|(
literal|") : "
argument_list|)
expr_stmt|;
break|break;
block|}
name|message
operator|.
name|append
argument_list|(
name|info
operator|==
literal|null
condition|?
literal|"null"
else|:
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|message
operator|.
name|toString
argument_list|()
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Writable
comment|//////////////////////////////////////////////////////////////////////////////
comment|/**    * {@inheritDoc}    */
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
name|out
operator|.
name|writeByte
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|info
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
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
name|msg
operator|=
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|this
operator|.
name|info
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

