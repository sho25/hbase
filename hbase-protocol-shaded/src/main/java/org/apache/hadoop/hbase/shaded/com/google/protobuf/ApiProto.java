begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Generated by the protocol buffer compiler.  DO NOT EDIT!
end_comment

begin_comment
comment|// source: google/protobuf/api.proto
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
package|;
end_package

begin_class
specifier|public
specifier|final
class|class
name|ApiProto
block|{
specifier|private
name|ApiProto
parameter_list|()
block|{}
specifier|public
specifier|static
name|void
name|registerAllExtensions
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
name|registry
parameter_list|)
block|{   }
specifier|public
specifier|static
name|void
name|registerAllExtensions
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistry
name|registry
parameter_list|)
block|{
name|registerAllExtensions
argument_list|(
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
operator|)
name|registry
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_google_protobuf_Api_descriptor
decl_stmt|;
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
name|internal_static_google_protobuf_Api_fieldAccessorTable
decl_stmt|;
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_google_protobuf_Method_descriptor
decl_stmt|;
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
name|internal_static_google_protobuf_Method_fieldAccessorTable
decl_stmt|;
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_google_protobuf_Mixin_descriptor
decl_stmt|;
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
name|internal_static_google_protobuf_Mixin_fieldAccessorTable
decl_stmt|;
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|getDescriptor
parameter_list|()
block|{
return|return
name|descriptor
return|;
block|}
specifier|private
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|descriptor
decl_stmt|;
static|static
block|{
name|java
operator|.
name|lang
operator|.
name|String
index|[]
name|descriptorData
init|=
block|{
literal|"\n\031google/protobuf/api.proto\022\017google.prot"
operator|+
literal|"obuf\032$google/protobuf/source_context.pro"
operator|+
literal|"to\032\032google/protobuf/type.proto\"\201\002\n\003Api\022\014"
operator|+
literal|"\n\004name\030\001 \001(\t\022(\n\007methods\030\002 \003(\0132\027.google.p"
operator|+
literal|"rotobuf.Method\022(\n\007options\030\003 \003(\0132\027.google"
operator|+
literal|".protobuf.Option\022\017\n\007version\030\004 \001(\t\0226\n\016sou"
operator|+
literal|"rce_context\030\005 \001(\0132\036.google.protobuf.Sour"
operator|+
literal|"ceContext\022&\n\006mixins\030\006 \003(\0132\026.google.proto"
operator|+
literal|"buf.Mixin\022\'\n\006syntax\030\007 \001(\0162\027.google.proto"
operator|+
literal|"buf.Syntax\"\325\001\n\006Method\022\014\n\004name\030\001 \001(\t\022\030\n\020r"
block|,
literal|"equest_type_url\030\002 \001(\t\022\031\n\021request_streami"
operator|+
literal|"ng\030\003 \001(\010\022\031\n\021response_type_url\030\004 \001(\t\022\032\n\022r"
operator|+
literal|"esponse_streaming\030\005 \001(\010\022(\n\007options\030\006 \003(\013"
operator|+
literal|"2\027.google.protobuf.Option\022\'\n\006syntax\030\007 \001("
operator|+
literal|"\0162\027.google.protobuf.Syntax\"#\n\005Mixin\022\014\n\004n"
operator|+
literal|"ame\030\001 \001(\t\022\014\n\004root\030\002 \001(\tBu\n\023com.google.pr"
operator|+
literal|"otobufB\010ApiProtoP\001Z+google.golang.org/ge"
operator|+
literal|"nproto/protobuf/api;api\242\002\003GPB\252\002\036Google.P"
operator|+
literal|"rotobuf.WellKnownTypesb\006proto3"
block|}
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|InternalDescriptorAssigner
name|assigner
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|InternalDescriptorAssigner
argument_list|()
block|{
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistry
name|assignDescriptors
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|root
parameter_list|)
block|{
name|descriptor
operator|=
name|root
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|internalBuildGeneratedFileFrom
argument_list|(
name|descriptorData
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
index|[]
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|SourceContextProto
operator|.
name|getDescriptor
argument_list|()
block|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|TypeProto
operator|.
name|getDescriptor
argument_list|()
block|,         }
argument_list|,
name|assigner
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Api_descriptor
operator|=
name|getDescriptor
argument_list|()
operator|.
name|getMessageTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Api_fieldAccessorTable
operator|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_google_protobuf_Api_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Name"
block|,
literal|"Methods"
block|,
literal|"Options"
block|,
literal|"Version"
block|,
literal|"SourceContext"
block|,
literal|"Mixins"
block|,
literal|"Syntax"
block|, }
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Method_descriptor
operator|=
name|getDescriptor
argument_list|()
operator|.
name|getMessageTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Method_fieldAccessorTable
operator|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_google_protobuf_Method_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Name"
block|,
literal|"RequestTypeUrl"
block|,
literal|"RequestStreaming"
block|,
literal|"ResponseTypeUrl"
block|,
literal|"ResponseStreaming"
block|,
literal|"Options"
block|,
literal|"Syntax"
block|, }
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Mixin_descriptor
operator|=
name|getDescriptor
argument_list|()
operator|.
name|getMessageTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Mixin_fieldAccessorTable
operator|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_google_protobuf_Mixin_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Name"
block|,
literal|"Root"
block|, }
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|SourceContextProto
operator|.
name|getDescriptor
argument_list|()
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|TypeProto
operator|.
name|getDescriptor
argument_list|()
expr_stmt|;
block|}
comment|// @@protoc_insertion_point(outer_class_scope)
block|}
end_class

end_unit

