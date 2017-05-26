// Code generated by protoc-gen-go. DO NOT EDIT.
// source: wire.proto

/*
Package main is a generated protocol buffer package.

It is generated from these files:
	wire.proto

It has these top-level messages:
	Login
	Contact
	Text
	File
	Time
	Timestamp
	Image
	FormatDescription
	VideoSample
	AudioSample
	Av
	Haber
*/
package main

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

// Identifies which field is filled in
type Haber_Which int32

const (
	Haber_LOGIN    Haber_Which = 0
	Haber_CONTACTS Haber_Which = 1
	Haber_PRESENCE Haber_Which = 2
	Haber_TEXT     Haber_Which = 3
	Haber_FILE     Haber_Which = 4
	Haber_AV       Haber_Which = 5
)

var Haber_Which_name = map[int32]string{
	0: "LOGIN",
	1: "CONTACTS",
	2: "PRESENCE",
	3: "TEXT",
	4: "FILE",
	5: "AV",
}
var Haber_Which_value = map[string]int32{
	"LOGIN":    0,
	"CONTACTS": 1,
	"PRESENCE": 2,
	"TEXT":     3,
	"FILE":     4,
	"AV":       5,
}

func (x Haber_Which) String() string {
	return proto.EnumName(Haber_Which_name, int32(x))
}
func (Haber_Which) EnumDescriptor() ([]byte, []int) { return fileDescriptor0, []int{11, 0} }

type Login struct {
	Username string `protobuf:"bytes,1,opt,name=username" json:"username,omitempty"`
}

func (m *Login) Reset()                    { *m = Login{} }
func (m *Login) String() string            { return proto.CompactTextString(m) }
func (*Login) ProtoMessage()               {}
func (*Login) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{0} }

func (m *Login) GetUsername() string {
	if m != nil {
		return m.Username
	}
	return ""
}

type Contact struct {
	Name   string `protobuf:"bytes,1,opt,name=name" json:"name,omitempty"`
	Online bool   `protobuf:"varint,2,opt,name=online" json:"online,omitempty"`
}

func (m *Contact) Reset()                    { *m = Contact{} }
func (m *Contact) String() string            { return proto.CompactTextString(m) }
func (*Contact) ProtoMessage()               {}
func (*Contact) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{1} }

func (m *Contact) GetName() string {
	if m != nil {
		return m.Name
	}
	return ""
}

func (m *Contact) GetOnline() bool {
	if m != nil {
		return m.Online
	}
	return false
}

type Text struct {
	Body string `protobuf:"bytes,1,opt,name=body" json:"body,omitempty"`
}

func (m *Text) Reset()                    { *m = Text{} }
func (m *Text) String() string            { return proto.CompactTextString(m) }
func (*Text) ProtoMessage()               {}
func (*Text) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{2} }

func (m *Text) GetBody() string {
	if m != nil {
		return m.Body
	}
	return ""
}

type File struct {
	Key  string `protobuf:"bytes,1,opt,name=key" json:"key,omitempty"`
	Data []byte `protobuf:"bytes,2,opt,name=data,proto3" json:"data,omitempty"`
}

func (m *File) Reset()                    { *m = File{} }
func (m *File) String() string            { return proto.CompactTextString(m) }
func (*File) ProtoMessage()               {}
func (*File) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{3} }

func (m *File) GetKey() string {
	if m != nil {
		return m.Key
	}
	return ""
}

func (m *File) GetData() []byte {
	if m != nil {
		return m.Data
	}
	return nil
}

type Time struct {
	Value int64  `protobuf:"varint,1,opt,name=value" json:"value,omitempty"`
	Scale int32  `protobuf:"varint,2,opt,name=scale" json:"scale,omitempty"`
	Flags uint32 `protobuf:"varint,3,opt,name=flags" json:"flags,omitempty"`
	Epoch int64  `protobuf:"varint,4,opt,name=epoch" json:"epoch,omitempty"`
}

func (m *Time) Reset()                    { *m = Time{} }
func (m *Time) String() string            { return proto.CompactTextString(m) }
func (*Time) ProtoMessage()               {}
func (*Time) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{4} }

func (m *Time) GetValue() int64 {
	if m != nil {
		return m.Value
	}
	return 0
}

func (m *Time) GetScale() int32 {
	if m != nil {
		return m.Scale
	}
	return 0
}

func (m *Time) GetFlags() uint32 {
	if m != nil {
		return m.Flags
	}
	return 0
}

func (m *Time) GetEpoch() int64 {
	if m != nil {
		return m.Epoch
	}
	return 0
}

type Timestamp struct {
	Duration     *Time `protobuf:"bytes,1,opt,name=duration" json:"duration,omitempty"`
	Presentation *Time `protobuf:"bytes,2,opt,name=presentation" json:"presentation,omitempty"`
}

func (m *Timestamp) Reset()                    { *m = Timestamp{} }
func (m *Timestamp) String() string            { return proto.CompactTextString(m) }
func (*Timestamp) ProtoMessage()               {}
func (*Timestamp) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{5} }

func (m *Timestamp) GetDuration() *Time {
	if m != nil {
		return m.Duration
	}
	return nil
}

func (m *Timestamp) GetPresentation() *Time {
	if m != nil {
		return m.Presentation
	}
	return nil
}

type Image struct {
	Width       int64             `protobuf:"varint,1,opt,name=width" json:"width,omitempty"`
	Height      int64             `protobuf:"varint,2,opt,name=height" json:"height,omitempty"`
	Format      uint32            `protobuf:"varint,3,opt,name=format" json:"format,omitempty"`
	Attachments map[string]string `protobuf:"bytes,4,rep,name=attachments" json:"attachments,omitempty" protobuf_key:"bytes,1,opt,name=key" protobuf_val:"bytes,2,opt,name=value"`
	Data        []byte            `protobuf:"bytes,5,opt,name=data,proto3" json:"data,omitempty"`
}

func (m *Image) Reset()                    { *m = Image{} }
func (m *Image) String() string            { return proto.CompactTextString(m) }
func (*Image) ProtoMessage()               {}
func (*Image) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{6} }

func (m *Image) GetWidth() int64 {
	if m != nil {
		return m.Width
	}
	return 0
}

func (m *Image) GetHeight() int64 {
	if m != nil {
		return m.Height
	}
	return 0
}

func (m *Image) GetFormat() uint32 {
	if m != nil {
		return m.Format
	}
	return 0
}

func (m *Image) GetAttachments() map[string]string {
	if m != nil {
		return m.Attachments
	}
	return nil
}

func (m *Image) GetData() []byte {
	if m != nil {
		return m.Data
	}
	return nil
}

type FormatDescription struct {
	MediaType    uint32            `protobuf:"varint,1,opt,name=mediaType" json:"mediaType,omitempty"`
	MediaSubtype uint32            `protobuf:"varint,2,opt,name=mediaSubtype" json:"mediaSubtype,omitempty"`
	Extensions   map[string]string `protobuf:"bytes,3,rep,name=extensions" json:"extensions,omitempty" protobuf_key:"bytes,1,opt,name=key" protobuf_val:"bytes,2,opt,name=value"`
}

func (m *FormatDescription) Reset()                    { *m = FormatDescription{} }
func (m *FormatDescription) String() string            { return proto.CompactTextString(m) }
func (*FormatDescription) ProtoMessage()               {}
func (*FormatDescription) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{7} }

func (m *FormatDescription) GetMediaType() uint32 {
	if m != nil {
		return m.MediaType
	}
	return 0
}

func (m *FormatDescription) GetMediaSubtype() uint32 {
	if m != nil {
		return m.MediaSubtype
	}
	return 0
}

func (m *FormatDescription) GetExtensions() map[string]string {
	if m != nil {
		return m.Extensions
	}
	return nil
}

type VideoSample struct {
	Image *Image `protobuf:"bytes,1,opt,name=image" json:"image,omitempty"`
}

func (m *VideoSample) Reset()                    { *m = VideoSample{} }
func (m *VideoSample) String() string            { return proto.CompactTextString(m) }
func (*VideoSample) ProtoMessage()               {}
func (*VideoSample) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{8} }

func (m *VideoSample) GetImage() *Image {
	if m != nil {
		return m.Image
	}
	return nil
}

type AudioSample struct {
	Image *Image `protobuf:"bytes,1,opt,name=image" json:"image,omitempty"`
}

func (m *AudioSample) Reset()                    { *m = AudioSample{} }
func (m *AudioSample) String() string            { return proto.CompactTextString(m) }
func (*AudioSample) ProtoMessage()               {}
func (*AudioSample) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{9} }

func (m *AudioSample) GetImage() *Image {
	if m != nil {
		return m.Image
	}
	return nil
}

type Av struct {
	Video *VideoSample `protobuf:"bytes,1,opt,name=video" json:"video,omitempty"`
	Audio *AudioSample `protobuf:"bytes,2,opt,name=audio" json:"audio,omitempty"`
}

func (m *Av) Reset()                    { *m = Av{} }
func (m *Av) String() string            { return proto.CompactTextString(m) }
func (*Av) ProtoMessage()               {}
func (*Av) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{10} }

func (m *Av) GetVideo() *VideoSample {
	if m != nil {
		return m.Video
	}
	return nil
}

func (m *Av) GetAudio() *AudioSample {
	if m != nil {
		return m.Audio
	}
	return nil
}

type Haber struct {
	Version   uint32      `protobuf:"varint,1,opt,name=version" json:"version,omitempty"`
	SessionId string      `protobuf:"bytes,2,opt,name=sessionId" json:"sessionId,omitempty"`
	From      string      `protobuf:"bytes,3,opt,name=from" json:"from,omitempty"`
	To        string      `protobuf:"bytes,4,opt,name=to" json:"to,omitempty"`
	Which     Haber_Which `protobuf:"varint,5,opt,name=which,enum=Haber_Which" json:"which,omitempty"`
	// One of the following will be filled in
	Login    *Login     `protobuf:"bytes,101,opt,name=login" json:"login,omitempty"`
	Contacts []*Contact `protobuf:"bytes,102,rep,name=contacts" json:"contacts,omitempty"`
	Text     *Text      `protobuf:"bytes,104,opt,name=text" json:"text,omitempty"`
	Av       *Av        `protobuf:"bytes,105,opt,name=av" json:"av,omitempty"`
	File     *File      `protobuf:"bytes,106,opt,name=file" json:"file,omitempty"`
}

func (m *Haber) Reset()                    { *m = Haber{} }
func (m *Haber) String() string            { return proto.CompactTextString(m) }
func (*Haber) ProtoMessage()               {}
func (*Haber) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{11} }

func (m *Haber) GetVersion() uint32 {
	if m != nil {
		return m.Version
	}
	return 0
}

func (m *Haber) GetSessionId() string {
	if m != nil {
		return m.SessionId
	}
	return ""
}

func (m *Haber) GetFrom() string {
	if m != nil {
		return m.From
	}
	return ""
}

func (m *Haber) GetTo() string {
	if m != nil {
		return m.To
	}
	return ""
}

func (m *Haber) GetWhich() Haber_Which {
	if m != nil {
		return m.Which
	}
	return Haber_LOGIN
}

func (m *Haber) GetLogin() *Login {
	if m != nil {
		return m.Login
	}
	return nil
}

func (m *Haber) GetContacts() []*Contact {
	if m != nil {
		return m.Contacts
	}
	return nil
}

func (m *Haber) GetText() *Text {
	if m != nil {
		return m.Text
	}
	return nil
}

func (m *Haber) GetAv() *Av {
	if m != nil {
		return m.Av
	}
	return nil
}

func (m *Haber) GetFile() *File {
	if m != nil {
		return m.File
	}
	return nil
}

func init() {
	proto.RegisterType((*Login)(nil), "Login")
	proto.RegisterType((*Contact)(nil), "Contact")
	proto.RegisterType((*Text)(nil), "Text")
	proto.RegisterType((*File)(nil), "File")
	proto.RegisterType((*Time)(nil), "Time")
	proto.RegisterType((*Timestamp)(nil), "Timestamp")
	proto.RegisterType((*Image)(nil), "Image")
	proto.RegisterType((*FormatDescription)(nil), "FormatDescription")
	proto.RegisterType((*VideoSample)(nil), "VideoSample")
	proto.RegisterType((*AudioSample)(nil), "AudioSample")
	proto.RegisterType((*Av)(nil), "Av")
	proto.RegisterType((*Haber)(nil), "Haber")
	proto.RegisterEnum("Haber_Which", Haber_Which_name, Haber_Which_value)
}

func init() { proto.RegisterFile("wire.proto", fileDescriptor0) }

var fileDescriptor0 = []byte{
	// 703 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0x94, 0x54, 0xcb, 0x6f, 0xd3, 0x4e,
	0x10, 0xfe, 0xd9, 0xb1, 0xd3, 0x64, 0x92, 0xf6, 0x67, 0x16, 0x04, 0x26, 0xea, 0x21, 0x18, 0x0e,
	0x41, 0x20, 0x1f, 0x8a, 0x90, 0x00, 0x09, 0xa4, 0x34, 0xa4, 0x10, 0x14, 0xb5, 0x68, 0x13, 0x95,
	0xc7, 0x01, 0x69, 0x13, 0x6f, 0xe2, 0x05, 0x3f, 0x22, 0x7b, 0xe3, 0xa6, 0xff, 0x29, 0x07, 0x4e,
	0xfc, 0x25, 0x68, 0x67, 0x9d, 0x47, 0x0b, 0x07, 0xb8, 0xcd, 0xf7, 0xcd, 0xe7, 0x79, 0xed, 0x78,
	0x00, 0x2e, 0x44, 0xc6, 0xfd, 0x45, 0x96, 0xca, 0xd4, 0xbb, 0x0f, 0xf6, 0x30, 0x9d, 0x8b, 0x84,
	0xb4, 0xa0, 0xb6, 0xcc, 0x79, 0x96, 0xb0, 0x98, 0xbb, 0x46, 0xdb, 0xe8, 0xd4, 0xe9, 0x06, 0x7b,
	0x4f, 0x61, 0xaf, 0x97, 0x26, 0x92, 0x4d, 0x25, 0x21, 0x60, 0xed, 0x48, 0xd0, 0x26, 0xb7, 0xa1,
	0x9a, 0x26, 0x91, 0x48, 0xb8, 0x6b, 0xb6, 0x8d, 0x4e, 0x8d, 0x96, 0xc8, 0x6b, 0x81, 0x35, 0xe6,
	0x2b, 0xfc, 0x66, 0x92, 0x06, 0x97, 0xeb, 0x6f, 0x94, 0xed, 0x3d, 0x06, 0xeb, 0x44, 0x44, 0x9c,
	0x38, 0x50, 0xf9, 0xc6, 0xd7, 0x2e, 0x65, 0x2a, 0x75, 0xc0, 0x24, 0xc3, 0x58, 0x4d, 0x8a, 0xb6,
	0xf7, 0x05, 0xac, 0xb1, 0x88, 0x39, 0xb9, 0x05, 0x76, 0xc1, 0xa2, 0xa5, 0x4e, 0x5f, 0xa1, 0x1a,
	0x28, 0x36, 0x9f, 0xb2, 0x48, 0xa7, 0xb7, 0xa9, 0x06, 0x8a, 0x9d, 0x45, 0x6c, 0x9e, 0xbb, 0x95,
	0xb6, 0xd1, 0xd9, 0xa7, 0x1a, 0x28, 0x96, 0x2f, 0xd2, 0x69, 0xe8, 0x5a, 0x3a, 0x02, 0x02, 0xef,
	0x13, 0xd4, 0x55, 0xfc, 0x5c, 0xb2, 0x78, 0x41, 0xee, 0x41, 0x2d, 0x58, 0x66, 0x4c, 0x8a, 0x34,
	0xc1, 0x3c, 0x8d, 0x23, 0xdb, 0x57, 0x5e, 0xba, 0xa1, 0xc9, 0x43, 0x68, 0x2e, 0x32, 0x9e, 0xf3,
	0x44, 0x6a, 0x99, 0xb9, 0x2b, 0xbb, 0xe2, 0xf2, 0x7e, 0x18, 0x60, 0x0f, 0x62, 0x36, 0xc7, 0x82,
	0x2e, 0x44, 0x20, 0xc3, 0x75, 0xf1, 0x08, 0xd4, 0xf0, 0x42, 0x2e, 0xe6, 0xa1, 0xc4, 0x20, 0x15,
	0x5a, 0x22, 0xc5, 0xcf, 0xd2, 0x2c, 0x66, 0xb2, 0xac, 0xbf, 0x44, 0xe4, 0x39, 0x34, 0x98, 0x94,
	0x6c, 0x1a, 0xc6, 0x3c, 0x91, 0xb9, 0x6b, 0xb5, 0x2b, 0x9d, 0xc6, 0xd1, 0x1d, 0x1f, 0x53, 0xf8,
	0xdd, 0xad, 0xa7, 0x9f, 0xc8, 0xec, 0x92, 0xee, 0x6a, 0x37, 0x93, 0xb5, 0xb7, 0x93, 0x6d, 0xbd,
	0x02, 0xe7, 0xfa, 0x47, 0x7f, 0x78, 0x93, 0xcd, 0xdc, 0x4d, 0xe4, 0x34, 0x78, 0x61, 0x3e, 0x33,
	0xbc, 0xef, 0x06, 0xdc, 0x38, 0xc1, 0xca, 0x5e, 0xf3, 0x7c, 0x9a, 0x89, 0x05, 0xce, 0xe7, 0x10,
	0xea, 0x31, 0x0f, 0x04, 0x1b, 0x5f, 0x2e, 0xf4, 0x5b, 0xed, 0xd3, 0x2d, 0x41, 0x3c, 0x68, 0x22,
	0x18, 0x2d, 0x27, 0x52, 0x09, 0x4c, 0x14, 0x5c, 0xe1, 0xc8, 0x31, 0x00, 0x5f, 0x49, 0x9e, 0xe4,
	0x22, 0x4d, 0xd4, 0x13, 0xaa, 0x2e, 0x3d, 0xff, 0xb7, 0x4c, 0x7e, 0x7f, 0x23, 0xd2, 0x0d, 0xef,
	0x7c, 0xd5, 0x7a, 0x09, 0xff, 0x5f, 0x73, 0xff, 0x53, 0x6b, 0x8f, 0xa0, 0x71, 0x2e, 0x02, 0x9e,
	0x8e, 0x58, 0xbc, 0x88, 0x38, 0x39, 0x04, 0x5b, 0xa8, 0x21, 0x97, 0x3b, 0x51, 0xd5, 0x23, 0xa7,
	0x9a, 0x54, 0xe2, 0xee, 0x32, 0x10, 0x7f, 0x27, 0x1e, 0x82, 0xd9, 0x2d, 0x88, 0x07, 0x76, 0xa1,
	0xe2, 0x97, 0x9a, 0xa6, 0xbf, 0x93, 0x8d, 0x6a, 0x97, 0xd2, 0x30, 0x15, 0xb6, 0xdc, 0xb0, 0xa6,
	0xbf, 0x93, 0x84, 0x6a, 0x97, 0xf7, 0xd3, 0x04, 0xfb, 0x2d, 0x9b, 0xf0, 0x8c, 0xb8, 0xb0, 0x57,
	0xf0, 0x2c, 0x5f, 0x2f, 0xee, 0x3e, 0x5d, 0x43, 0xf5, 0x20, 0x39, 0xcf, 0x95, 0x39, 0x08, 0xca,
	0x4e, 0xb7, 0x84, 0x5a, 0x8c, 0x59, 0x96, 0xc6, 0xb8, 0x69, 0x75, 0x8a, 0x36, 0x39, 0x00, 0x53,
	0xa6, 0xf8, 0x97, 0xd4, 0xa9, 0x29, 0xb1, 0x92, 0x8b, 0x50, 0x4c, 0x43, 0xdc, 0x9e, 0x83, 0xa3,
	0xa6, 0x8f, 0x29, 0xfd, 0x0f, 0x8a, 0xa3, 0xda, 0xa5, 0xba, 0x8e, 0xd4, 0x31, 0x71, 0x79, 0xd9,
	0x35, 0x9e, 0x16, 0xaa, 0x49, 0xf2, 0x00, 0x6a, 0x53, 0x7d, 0x45, 0x72, 0x77, 0x86, 0x0f, 0x5a,
	0xf3, 0xcb, 0xb3, 0x42, 0x37, 0x1e, 0x72, 0x17, 0x2c, 0xc9, 0x57, 0xd2, 0x0d, 0xd7, 0xbf, 0x14,
	0x5f, 0x49, 0x8a, 0x14, 0xb9, 0x09, 0x26, 0x2b, 0x5c, 0x81, 0x8e, 0x8a, 0xdf, 0x2d, 0xa8, 0xc9,
	0x0a, 0xa5, 0x9f, 0x89, 0x88, 0xbb, 0x5f, 0x4b, 0xbd, 0xba, 0x2a, 0x14, 0x29, 0xef, 0x1d, 0xd8,
	0x58, 0x1e, 0xa9, 0x83, 0x3d, 0x3c, 0x7b, 0x33, 0x38, 0x75, 0xfe, 0x23, 0x4d, 0xa8, 0xf5, 0xce,
	0x4e, 0xc7, 0xdd, 0xde, 0x78, 0xe4, 0x18, 0x0a, 0xbd, 0xa7, 0xfd, 0x51, 0xff, 0xb4, 0xd7, 0x77,
	0x4c, 0x52, 0x03, 0x6b, 0xdc, 0xff, 0x38, 0x76, 0x2a, 0xca, 0x3a, 0x19, 0x0c, 0xfb, 0x8e, 0x45,
	0xaa, 0x60, 0x76, 0xcf, 0x1d, 0xfb, 0xb8, 0xfa, 0xd9, 0x8a, 0x99, 0x48, 0x26, 0x55, 0x3c, 0x9b,
	0x4f, 0x7e, 0x05, 0x00, 0x00, 0xff, 0xff, 0x66, 0xd1, 0x2c, 0xd7, 0x44, 0x05, 0x00, 0x00,
}
