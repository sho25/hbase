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
name|serializer
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|io
operator|.
name|Cell
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
name|io
operator|.
name|RowResult
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
name|rest
operator|.
name|DatabaseModel
operator|.
name|DatabaseMetadata
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
name|rest
operator|.
name|Status
operator|.
name|StatusMessage
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
name|rest
operator|.
name|TableModel
operator|.
name|Regions
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
name|rest
operator|.
name|descriptors
operator|.
name|ScannerIdentifier
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
name|rest
operator|.
name|descriptors
operator|.
name|TimestampsDescriptor
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
name|rest
operator|.
name|exception
operator|.
name|HBaseRestException
import|;
end_import

begin_import
import|import
name|agilejson
operator|.
name|JSON
import|;
end_import

begin_comment
comment|/**  *   * Serializes objects into JSON strings and prints them back out on the output  * stream. It should be noted that this JSON implementation uses annotations on  * the objects to be serialized.  *   * Since these annotations are used to describe the serialization of the objects  * the only method that is implemented is writeOutput(Object o). The other  * methods in the interface do not need to be implemented.  */
end_comment

begin_class
specifier|public
class|class
name|JSONSerializer
extends|extends
name|AbstractRestSerializer
block|{
comment|/**    * @param response    */
specifier|public
name|JSONSerializer
parameter_list|(
name|HttpServletResponse
name|response
parameter_list|)
block|{
name|super
argument_list|(
name|response
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#writeOutput(java    * .lang.Object, javax.servlet.http.HttpServletResponse)    */
specifier|public
name|void
name|writeOutput
parameter_list|(
name|Object
name|o
parameter_list|)
throws|throws
name|HBaseRestException
block|{
name|response
operator|.
name|setContentType
argument_list|(
literal|"application/json"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// LOG.debug("At top of send data");
name|String
name|data
init|=
name|JSON
operator|.
name|toJSON
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|response
operator|.
name|setContentLength
argument_list|(
name|data
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|println
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// LOG.debug("Error sending data: " + e.toString());
throw|throw
operator|new
name|HBaseRestException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/*    * (non-Javadoc)    *     * @seeorg.apache.hadoop.hbase.rest.serializer.IRestSerializer#    * serializeColumnDescriptor(org.apache.hadoop.hbase.HColumnDescriptor)    */
specifier|public
name|void
name|serializeColumnDescriptor
parameter_list|(
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @seeorg.apache.hadoop.hbase.rest.serializer.IRestSerializer#    * serializeDatabaseMetadata    * (org.apache.hadoop.hbase.rest.DatabaseModel.DatabaseMetadata)    */
specifier|public
name|void
name|serializeDatabaseMetadata
parameter_list|(
name|DatabaseMetadata
name|databaseMetadata
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#serializeRegionData    * (org.apache.hadoop.hbase.rest.TableModel.Regions)    */
specifier|public
name|void
name|serializeRegionData
parameter_list|(
name|Regions
name|regions
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @seeorg.apache.hadoop.hbase.rest.serializer.IRestSerializer#    * serializeTableDescriptor(org.apache.hadoop.hbase.HTableDescriptor)    */
specifier|public
name|void
name|serializeTableDescriptor
parameter_list|(
name|HTableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#serializeStatusMessage    * (org.apache.hadoop.hbase.rest.Status.StatusMessage)    */
specifier|public
name|void
name|serializeStatusMessage
parameter_list|(
name|StatusMessage
name|message
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @seeorg.apache.hadoop.hbase.rest.serializer.IRestSerializer#    * serializeScannerIdentifier(org.apache.hadoop.hbase.rest.ScannerIdentifier)    */
specifier|public
name|void
name|serializeScannerIdentifier
parameter_list|(
name|ScannerIdentifier
name|scannerIdentifier
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#serializeRowResult    * (org.apache.hadoop.hbase.io.RowResult)    */
specifier|public
name|void
name|serializeRowResult
parameter_list|(
name|RowResult
name|rowResult
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#serializeRowResultArray    * (org.apache.hadoop.hbase.io.RowResult[])    */
specifier|public
name|void
name|serializeRowResultArray
parameter_list|(
name|RowResult
index|[]
name|rows
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#serializeCell(org    * .apache.hadoop.hbase.io.Cell)    */
specifier|public
name|void
name|serializeCell
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#serializeCellArray    * (org.apache.hadoop.hbase.io.Cell[])    */
specifier|public
name|void
name|serializeCellArray
parameter_list|(
name|Cell
index|[]
name|cells
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hbase.rest.serializer.IRestSerializer#serializeTimestamps    * (org.apache.hadoop.hbase.rest.RowModel.TimestampsDescriptor)    */
specifier|public
name|void
name|serializeTimestamps
parameter_list|(
name|TimestampsDescriptor
name|timestampsDescriptor
parameter_list|)
throws|throws
name|HBaseRestException
block|{
comment|// No implementation needed for the JSON serializer
block|}
block|}
end_class

end_unit

